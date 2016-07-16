package if3t.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import if3t.exceptions.AddRecipeException;
import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.NoPermissionException;
import if3t.exceptions.NotLoggedInException;
import if3t.models.Action;
import if3t.models.ActionIngredient;
import if3t.models.ParametersActions;
import if3t.models.ParametersTriggers;
import if3t.models.Recipe;
import if3t.models.Response;
import if3t.models.Trigger;
import if3t.models.TriggerIngredient;
import if3t.models.User;
import if3t.services.ActionService;
import if3t.services.CreateRecipeService;
import if3t.services.RecipeService;
import if3t.services.TriggerService;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class RecipeController {

	@Autowired
	private RecipeService recipeService;
	@Autowired
	private UserService userService;
	@Autowired
	private ActionService actionService;	
	@Autowired
	private TriggerService triggerService;
	@Autowired
	private CreateRecipeService createRecipeService;
	
	private static final String EMAIL_PATTERN = 
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	
	@RequestMapping(value="/user_recipes", method=RequestMethod.GET)
	public List<Recipe> getUserRecipes() throws NotLoggedInException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth == null)
			throw new NotLoggedInException("ERROR: not loggedIn");
		
		user = userService.getUserByUsername(auth.getName());
		return recipeService.readUserRecipes(user.getId());
	}
	
	@RequestMapping(value="/public_recipes", method=RequestMethod.GET)
	public List<Recipe> getPublicRecipes() {
		return recipeService.readPublicRecipes();
	}
	
	@RequestMapping(value="/recipe/{id}", method=RequestMethod.GET)
	public List<Recipe> readRecipe(@PathVariable Long id) throws NotLoggedInException, NoPermissionException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User loggedUser = null;
		if (auth == null)
			throw new NotLoggedInException("ERROR: not loggedIn");
		
		loggedUser = userService.getUserByUsername(auth.getName());
		List<Recipe> recipeList  = recipeService.readRecipe(id, loggedUser);
		
		return recipeList;
	}
	
	@RequestMapping(value="/add_recipe", method=RequestMethod.POST)
	public void addRecipe(@RequestBody List<Recipe> recipe) throws NotLoggedInException, AddRecipeException {
		//TODO controlli user
		System.out.println("ADDING RECIPE, CONTROLLING THE RECIPE LIST");
		//checks on recipes
		for(Recipe r : recipe){
			System.out.println("CONTROLLING A RECIPE");
			if(r.getAction()!=null && r.getAction_ingredients()!=null && r.getTrigger()!=null && r.getTrigger_ingredients()!=null && r.getDescription()!=null){
				System.out.println("ALL FIELDS ARE DIFFERENT FROM NULL");
				//check if parameters are valid
				boolean are_all_instances = true;
				boolean are_all_valid = true;
				if(r.getAction()instanceof Action && r.getTrigger()instanceof Trigger && r.getDescription()instanceof String)
					for(ActionIngredient ai : r.getAction_ingredients()){
						if(!(ai instanceof ActionIngredient)){
							are_all_instances = false;
							break;
						}
					}
					for(TriggerIngredient ti : r.getTrigger_ingredients()){
						if(!(ti instanceof TriggerIngredient)){
							are_all_instances = false;
							break;
						}
					}
					if(actionService.findById(r.getAction().getId())!=null
							&& triggerService.findById(r.getTrigger().getId())!=null
							){
						System.out.println("ACTION AND TRIGGER ARE VALID");
						for(TriggerIngredient ti : r.getTrigger_ingredients()){
							String type = ti.getParam().getType();							
							Object value = ti.getValue();
							switch(type){
								case "email" :  if(!is_email_type(value))
													are_all_valid = false;
												break;
								case "text" :	if(!is_text_type(value))
													are_all_valid = false;
												break;
								case "radio" :  if(!is_radio_type_triggers(value, ti))
													are_all_valid = false;	
												break;
								case "time" :  if(!is_time_type(value))
													are_all_valid = false;	
												break;
								case "date" :  if(!is_date_type(value))
													are_all_valid = false;	
												break;
								case "number" :  if(!is_number_type(value))
													are_all_valid = false;	
												break;
								case "checkbox" :  if(!is_checkbox_type_triggers(value, ti))
														are_all_valid = false;	
													break;
							}
							if(are_all_valid)
								System.out.println("This trigger ingredient is valid");
							else
								System.out.println("This trigger ingredient is not valid");
						}
						for(ActionIngredient ai : r.getAction_ingredients()){
							String type = ai.getParam().getType();							
							Object value = ai.getValue();
							switch(type){
								case "email" :  if(!is_email_type(value))
													are_all_valid = false;
												break;
								case "text" :	if(!is_text_type(value))
													are_all_valid = false;
												break;
								case "radio" :  if(!is_radio_type_actions(value, ai))
													are_all_valid = false;	
												break;
								case "time" :  if(!is_time_type(value))
													are_all_valid = false;	
												break;
								case "date" :  if(!is_date_type(value))
													are_all_valid = false;	
												break;
								case "number" :  if(!is_number_type(value))
													are_all_valid = false;	
												break;
								case "checkbox" :  if(!is_checkbox_type_actions(value, ai))
														are_all_valid = false;	
													break;
							}
							if(are_all_valid)
								System.out.println("This action ingredient is valid");
							else
								System.out.println("This action ingredient is not valid");
						}
					}
					if(are_all_instances && are_all_valid){
						System.out.println("Saving recipe");
						//check id parameters exist
						r.setIsEnabled(false);
						r.setIsPublic(false);		
						recipeService.addRecipe(recipe);
					}
					else{
						System.out.println("Invalid data in recipe sent");
						throw new AddRecipeException("ERROR: Invalid data in recipe sent");
					}
			}
			else{
				System.out.println("At leas one recipe sent misses a field");
				throw new AddRecipeException("ERROR: At leas one recipe sent misses a field");
			}
		}
	}
	
	private boolean is_number_type(Object value) {
		try{
			int i = (Integer) value;
		}
		catch(ClassCastException e){
			return false;			
		}
		return true;
	}

	private boolean is_date_type(Object value) {
		Date date = null;
		try {
		    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		    date = sdf.parse((String) value);
		    if (!value.equals(sdf.format(date))) {
		        date = null;
		    }
		} catch (ParseException ex) {
		    ex.printStackTrace();
		}
		if (date == null) {
			return false;
		} else {
		    return true;
		}
	}

	private boolean is_time_type(Object value) {
		Date date = null;
		try {
		    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		    date = sdf.parse((String) value);
		    if (!value.equals(sdf.format(date))) {
		        date = null;
		    }
		} catch (ParseException ex) {
		    ex.printStackTrace();
		}
		if (date == null) {
			return false;
		} else {
		    return true;
		}
	}
	
	private boolean is_radio_type_actions(Object value, ActionIngredient action_ingredient) {
		//it must have a value that is prevented by the radio button possibilities
		Long param_id = action_ingredient.getParam().getId();
		ParametersActions real_pa = createRecipeService.readParameterAction(param_id);
		String real_name = real_pa.getName();
		if(((String)value).equals(real_name) || ((String)value).equals("unchecked_radio_button")){
			return true;
		}
		return false;
	}
	
	private boolean is_checkbox_type_actions(Object value, ActionIngredient action_ingredient) {
		//it must have a value that is prevented by the radio button possibilities
		Long param_id = action_ingredient.getParam().getId();
		ParametersActions real_pa = createRecipeService.readParameterAction(param_id);
		String real_name = real_pa.getName();
		if(((String)value).equals(real_name) || ((String)value).equals("unchecked_radio_button")){
			return true;
		}
		return false;
	}

	private boolean is_radio_type_triggers(Object value, TriggerIngredient trigger_ingredient) {
		//it must have a value that is prevented by the radio button possibilities
		Long param_id = trigger_ingredient.getParam().getId();
		ParametersTriggers real_pt = createRecipeService.readParameterTrigger(param_id);
		String real_name = real_pt.getName();
		if(((String)value).equals(real_name) || ((String)value).equals("unchecked_radio_button")){
			return true;
		}
		return false;
	}
	
	private boolean is_checkbox_type_triggers(Object value, TriggerIngredient trigger_ingredient) {
		//it must have a value that is prevented by the radio button possibilities
		Long param_id = trigger_ingredient.getParam().getId();
		ParametersTriggers real_pt = createRecipeService.readParameterTrigger(param_id);
		String real_name = real_pt.getName();
		if(((String)value).equals(real_name) || ((String)value).equals("unchecked_radio_button")){
			return true;
		}
		return false;
	}
	
	private boolean is_text_type(Object value) {
		try{
			String s = new String((String) value);
		}
		catch(ClassCastException e){
			return false;			
		}
		return true;
	}

	private boolean is_email_type(Object value) {
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher((String)value);
		return matcher.matches();	
	}

	@RequestMapping(value="/remove_recipe/{id}", method=RequestMethod.POST)
	public void delRecipe(@PathVariable Long id) throws NotLoggedInException, NoPermissionException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User loggedUser = null;
		if (auth == null)
			throw new NotLoggedInException("ERROR: not loggedIn");
		
		loggedUser = userService.getUserByUsername(auth.getName());
		recipeService.deleteRecipe(id, loggedUser);
	}
	
	@RequestMapping(value="/publish_recipe/", method=RequestMethod.PUT)
	public Response publishRecipe(@RequestBody Recipe recipe) throws NotLoggedInException, NoPermissionException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not loggedIn");
		}
		
		User user = userService.getUserByUsername(auth.getName());
		List<Recipe> recipeList = recipeService.readRecipe(recipe.getId(), user);
		
		//Lo user è lo stesso (se non ci sono falle) in tutte le recipe (in cui cambia solo la action) quindi faccio un solo check
		if (!recipeList.get(0).getUser().equals(user)) {
			throw new NoPermissionException();
		}
				
		for (Recipe rec : recipeList)
		{
			recipeService.toggleIsPublicRecipe(rec);
		}
				
		return new Response("Successful", 200);
	}
	
	@RequestMapping(value="/enable_recipe", method=RequestMethod.PUT)
	public Response enableRecipe(@RequestBody Recipe recipe) throws NotLoggedInException, ChannelNotAuthorizedException, NoPermissionException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not loggedIn");
		}
		
		User user = userService.getUserByUsername(auth.getName());
		List<Recipe> recipeList = recipeService.readRecipe(recipe.getId(), user);
		
		//Lo user è lo stesso (se non ci sono falle) in tutte le recipe (in cui cambia solo la action) quindi faccio un solo check
		if (!recipeList.get(0).getUser().equals(user)) {
			throw new NoPermissionException();
		}
		
		for (Recipe rec : recipeList)
		{
			recipeService.toggleIsEnabledRecipe(rec, user);
		}
		
		return new Response("Successful", 200);
	}
}