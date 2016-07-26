package if3t.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import if3t.models.ActionPOJO;
import if3t.models.ParametersActions;
import if3t.models.ParametersTriggers;
import if3t.models.Recipe;
import if3t.models.RecipePOJO;
import if3t.models.Response;
import if3t.models.Trigger;
import if3t.models.TriggerIngredient;
import if3t.models.TriggerPOJO;
import if3t.models.User;
import if3t.services.ActionIngredientService;
import if3t.services.ActionService;
import if3t.services.CreateRecipeService;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;
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
	@Autowired
	private TriggerIngredientService triggerIngrService;
	@Autowired
	private ActionIngredientService actionIngrService;
	
	private static final String EMAIL_PATTERN = 
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	
	@RequestMapping(value="/user_recipes", method=RequestMethod.GET)
	public List<Recipe> getUserRecipes() throws NotLoggedInException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not loggedIn");
		}
		
		return recipeService.readUserRecipes(userService.getUserByUsername(auth.getName()).getId());
	}
	
	@RequestMapping(value="/public_recipes", method=RequestMethod.GET)
	public List<Recipe> getPublicRecipes() {
		return recipeService.readPublicRecipes();
	}
	
	@RequestMapping(value="/recipe/{id}", method=RequestMethod.GET)
	public RecipePOJO readRecipe(@PathVariable Long id) throws NotLoggedInException, NoPermissionException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not loggedIn");
		}
		
		User loggedUser = userService.getUserByUsername(auth.getName());
		List<Recipe> recList = recipeService.readRecipe(id, loggedUser);
		Trigger trig = recList.get(0).getTrigger();
		List<ParametersTriggers> ptList = createRecipeService.readChannelParametersTriggers(trig.getId(), trig.getChannel().getChannelId());
		Map<Long, TriggerIngredient> tiMap = triggerIngrService.getRecipeTriggerIngredientsMap(id);
		TriggerPOJO trigPOJO = new TriggerPOJO(trig, ptList, tiMap);
		List<ActionPOJO> actPOJOList = new ArrayList<ActionPOJO>();
		
		for (Recipe rec : recipeService.readRecipe(id, loggedUser))
		{
			Action act = rec.getAction();
			List<ParametersActions> paList = createRecipeService.readChannelParametersActions(act.getId(), act.getChannel().getChannelId());
			Map<Long, ActionIngredient> aiMap = actionIngrService.getRecipeActionIngredientsMap(rec.getId());
			actPOJOList.add(new ActionPOJO(act, paList, aiMap));
		}		
		
		return new RecipePOJO(recipeService.readRecipe(id, loggedUser), trigPOJO, actPOJOList);
	}
	
	//TODO aggiungere il tipo textarea nel db e il controllo qui
	//TODO pare non salvi gli ingredienti
	//TODO togliere il try catch alla fine quando è stabile
	@RequestMapping(value="/add_recipe", method=RequestMethod.POST)
	public void addRecipe(@RequestBody List<Recipe> recipe) throws NotLoggedInException, AddRecipeException 
	{		
		try
		{
			//TODO controlli user ???
			System.out.println("ADDING RECIPE, CONTROLLING THE RECIPE LIST");
			//checks on recipes
			for (Recipe r : recipe)
			{
				System.out.println("CONTROLLING A RECIPE");
				if (r.getAction() != null && r.getAction_ingredients() != null && r.getTrigger() != null && r.getTrigger_ingredients() != null && r.getDescription() != null)
				{
					System.out.println("ALL FIELDS ARE DIFFERENT FROM NULL");
					//check if parameters are valid
					boolean are_all_instances = true;
					boolean are_all_valid = true;
					//TODO aggiunte le parentesi a questo if che non sono certo mancassero per scelta considerando l'indentazione sotto
					if (r.getAction() instanceof Action && r.getTrigger() instanceof Trigger && r.getDescription() instanceof String)
					{
						for (ActionIngredient ai : r.getAction_ingredients())
						{
							if (!(ai instanceof ActionIngredient))
							{
								are_all_instances = false;
								break;
							}
						}
					
						for (TriggerIngredient ti : r.getTrigger_ingredients())
						{
							if (!(ti instanceof TriggerIngredient))
							{
								are_all_instances = false;
								break;
							}
						}
						
						if (actionService.findById(r.getAction().getId()) != null && triggerService.findById(r.getTrigger().getId()) != null)
						{
							System.out.println("ACTION AND TRIGGER ARE VALID");
							
							for (TriggerIngredient ti : r.getTrigger_ingredients())
							{
								if (!validate_ingredient(ti)) {
									are_all_valid = false;
								}
								
								if (are_all_valid) {
									System.out.println("This trigger ingredient is valid");
								}
								else {
									System.err.println(ti);
									System.err.println("This trigger ingredient is not valid");
								}
							}
							
							for (ActionIngredient ai : r.getAction_ingredients())
							{
								if (!validate_ingredient(ai)) {
									are_all_valid = false;
								}
								
								if (are_all_valid) {
									System.out.println("This action ingredient is valid");
								}
								else {
									System.err.println(ai);
									System.err.println("This action ingredient is not valid");
								}
							}
						}
						
						if (!are_all_instances || !are_all_valid)
						{
							System.out.println("Invalid data in recipe sent");
							throw new AddRecipeException("ERROR: Invalid data in recipe sent");
						}
					}
				}
				else
				{
					System.out.println("At least one recipe sent misses a field");
					throw new AddRecipeException("ERROR: At least one recipe sent misses a field");
				}
			}
			
			System.out.println("Saving recipe");
			recipeService.addRecipe(recipe);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private boolean is_text_type(Object value) {
		return value instanceof String;
	}
	
	private boolean is_number_type(Object value) {
		return value instanceof Integer;
	}

	private boolean is_date_time_type(Object value, String format)
	{
		if (value instanceof String)
		{
			Date date = null;
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			
			try 
			{
			    date = sdf.parse((String) value);
			    
			    if (!((String) value).equals(sdf.format(date))) {
			        date = null;
			    }
			} 
			catch (ParseException ex) 
			{
			    ex.printStackTrace();
			    return false;
			}
			
			return (date != null) ? true : false;
		}
		
		return false;
	}
	
	private boolean is_radio_type_or_checkbox_valid(Object value, Object ingredient)
	{
		Long param_id;
		String real_name;
		
		if (ingredient instanceof ActionIngredient)
		{
			param_id = ((ActionIngredient) ingredient).getParam().getId();
			ParametersActions real_pa = createRecipeService.readParameterAction(param_id);
			real_name = real_pa.getName();
			
		}
		else if (ingredient instanceof TriggerIngredient)
		{
			param_id = ((TriggerIngredient) ingredient).getParam().getId();
			ParametersTriggers real_pt = createRecipeService.readParameterTrigger(param_id);
			real_name = real_pt.getName();
		}
		else
		{
			return false;
		}
		
		return (((String) value).equals(real_name) || ((String) value).equals("unchecked_radio_button")) ? true : false;
	}
	
	private boolean validate_ingredient(Object ing)
	{
		String type = (ing instanceof ActionIngredient) ? ((ActionIngredient) ing).getParam().getType() : ((TriggerIngredient) ing).getParam().getType();		
		Object value = (ing instanceof ActionIngredient) ? ((ActionIngredient) ing).getValue() : ((TriggerIngredient) ing).getValue();
		
		switch (type)
		{
			case "email":	
				return is_email_type(value);
			case "text": case "textarea":	
				return is_text_type(value);					
			case "time":	
				return is_date_time_type(value, "HH:mm");
			case "date":  	
				return is_date_time_type(value, "dd/MM/yyyy");
			case "number":  
				return is_number_type(value);
			default: 		
				return is_radio_type_or_checkbox_valid(value, ing); 
		}
	}

	private boolean is_email_type(Object value) 
	{
		if (value instanceof String)
		{
			Pattern pattern = Pattern.compile(EMAIL_PATTERN);
			Matcher matcher = pattern.matcher((String) value);
			return matcher.matches();	
		}
		
		return false;
	}

	@RequestMapping(value="/remove_recipe/{id}", method=RequestMethod.POST)
	public void delRecipe(@PathVariable Long id) throws NotLoggedInException, NoPermissionException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null)
			throw new NotLoggedInException("ERROR: not loggedIn");
		
		recipeService.deleteRecipe(id, userService.getUserByUsername(auth.getName()));
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
	
	@RequestMapping(value="/enable_recipe/", method=RequestMethod.PUT)
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
		
		recipeService.toggleIsEnabledRecipe(recipeList, user);
		
		return new Response("Successful", 200);
	}
}