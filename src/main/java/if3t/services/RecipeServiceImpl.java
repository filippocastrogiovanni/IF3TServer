package if3t.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import if3t.exceptions.AddRecipeException;
import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.NoPermissionException;
import if3t.exceptions.NotFoundRecipeException;
import if3t.exceptions.PartialUpdateException;
import if3t.models.Action;
import if3t.models.ActionIngredient;
import if3t.models.ActionPOJO;
import if3t.models.Channel;
import if3t.models.ParametersActions;
import if3t.models.ParametersKeyword;
import if3t.models.ParametersPOJO;
import if3t.models.ParametersTriggers;
import if3t.models.Recipe;
import if3t.models.RecipePOJO;
import if3t.models.Trigger;
import if3t.models.TriggerIngredient;
import if3t.models.User;
import if3t.repositories.ActionIngredientRepository;
import if3t.repositories.AuthorizationRepository;
import if3t.repositories.ParameterKeywordRepository;
import if3t.repositories.RecipeRepository;
import if3t.repositories.TriggerIngredientRepository;

@Service
@Transactional
public class RecipeServiceImpl implements RecipeService {

	@Autowired
	private RecipeRepository recipeRepository;
	@Autowired
	private AuthorizationRepository authRepository;
	@Autowired
	private TriggerIngredientRepository triggerIngRepo;
	@Autowired
	private ActionIngredientRepository actionIngRepo;
	@Autowired
	private ActionService actionService;	
	@Autowired
	private TriggerService triggerService;
	@Autowired
	private ParameterKeywordService parameterKeywordService;
	@Autowired
	private CreateRecipeService createRecipeService;
	private static final String EMAIL_PATTERN = "^[-a-z0-9~!$%^&*_=+}{\'?]+(.[-a-z0-9~!$%^&*_=+}{\'?]+)*@([a-z0-9_][-a-z0-9_]*(.[-a-z0-9_]+)*.(aero|arpa|biz|com|coop|edu|gov|info|int|mil|museum|name|net|org|pro|travel|mobi|[a-z][a-z])|([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}))(:[0-9]{1,5})?$";
	
	//TODO controllare se quest'annotazione serve anche in altri services
	@PreAuthorize("hasRole('USER')")
	public List<Recipe> readUserRecipes(Long userId) {
		return recipeRepository.findByUser_Id(userId);
	}

	public List<Recipe> readPublicRecipes() {
		return recipeRepository.findByIsPublic(true);
	}

	@PreAuthorize("hasRole('USER')")
	public List<Recipe> readRecipe(Long id, User loggedUser) throws NoPermissionException, NotFoundRecipeException 
	{
		Recipe rec = recipeRepository.findOne(id);
		
		if (rec == null) {
			throw new NotFoundRecipeException("The requested recipe was not found");
		}		
		
		List<Recipe> recipeList = recipeRepository.findByGroupId(rec.getGroupId());
		
		for (Recipe recipe: recipeList)
		{
			if (!recipe.getUser().equals(loggedUser)) {
				throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
			}
		}
		
		return recipeList;
	}

	@PreAuthorize("hasRole('USER')")
	public void deleteRecipe(Long id, User loggedUser) throws NoPermissionException, NotFoundRecipeException 
	{
		Recipe recipe = recipeRepository.findOne(id);
		
		if (recipe == null) {
			throw new NotFoundRecipeException("The requested recipe was not found");
		}		
		
		if (!recipe.getUser().getId().equals(loggedUser.getId())) {
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		}
		
		for (Recipe rec : recipeRepository.findByGroupId(recipe.getGroupId()))
		{
			for (TriggerIngredient ti : rec.getTrigger_ingredients())
			{
				triggerIngRepo.delete(ti);
			}
			
			for (ActionIngredient ai : rec.getAction_ingredients())
			{
				actionIngRepo.delete(ai);
			}
			
			recipeRepository.delete(rec);
		}
	}

	@PreAuthorize("hasRole('USER')")
	public void addRecipe(List<Recipe> recipes, User loggedUser) throws AddRecipeException
	{		
		System.out.println("Saving recipe");
		validateRecipe(recipes);
		
		for (Recipe r : recipes)
		{
			r.setIsEnabled(false);
			r.setIsPublic(false);
			r.setGroupId(UUID.randomUUID().toString());	
			r.setUser(loggedUser);

			recipeRepository.save(r);

			if(r.getParameters_keyword()!=null)
				for(ParametersKeyword pk : r.getParameters_keyword()){
					pk.setRecipe(r);
					parameterKeywordService.addParametersKeyword(pk);
				}
			
			//Il salvataggio degli ingredienti va fatto qui (e non nel controller) per garantire la transazionalità
			for (TriggerIngredient ti : r.getTrigger_ingredients())
			{
				ti.setRecipe(r);
				triggerIngRepo.save(ti);
			}
			
			for (ActionIngredient ai : r.getAction_ingredients())
			{
				ai.setRecipe(r);
				actionIngRepo.save(ai);
			}
		}
	}

	@PreAuthorize("hasRole('USER')")
	public void toggleIsPublicRecipe(Recipe recipe) 
	{
		recipe.setIsPublic(!recipe.getIsPublic());
		recipeRepository.save(recipe);
	}

	@PreAuthorize("hasRole('USER')")
	public void toggleIsEnabledRecipe(List<Recipe> recipes, User user) throws ChannelNotAuthorizedException 
	{		
		Long userId = user.getId();
		Channel triggerChannel = recipes.get(0).getTrigger().getChannel();
		
		if (authRepository.findByUser_IdAndChannel_ChannelId(userId, triggerChannel.getChannelId()) == null) {
			throw new ChannelNotAuthorizedException("Trigger channel (" + triggerChannel.getName() + ") not authorized!");
		}
		
		Channel actionChannel;
		Set<Long> checkedActChannelIds = new HashSet<Long>();
		
		for (Recipe rec : recipes)
		{
			actionChannel = rec.getAction().getChannel();
			
			if (!checkedActChannelIds.contains(actionChannel.getChannelId())) 
			{
				if (authRepository.findByUser_IdAndChannel_ChannelId(userId, actionChannel.getChannelId()) == null) {
					throw new ChannelNotAuthorizedException("Action channel (" + actionChannel.getName() + ") not authorized!");
				}
				
				checkedActChannelIds.add(actionChannel.getChannelId());
			}
		}
		
		//TODO controllare questa affermazione che forse è falsa per via del @Transactional, quindi magari si può risparmiare un ciclo for
		//Solo se tutti i canali sono autorizzati vanno fatte le modifiche alle ricette altrimenti si potrebbero avere modifiche parziali
		for (Recipe rec : recipes)
		{
			rec.setIsEnabled(!rec.getIsEnabled());
			recipeRepository.save(rec);
		}
	}

	//TODO capire se serve a qualcosa
	public List<Recipe> getRecipeByTriggerChannel(String channelKeyword) {
		return recipeRepository.findByIsEnabledAndTrigger_Channel_Keyword(true, channelKeyword);
	}

	//FIXME println to remove
	@Override
	public void updateRecipe(RecipePOJO recipe) throws NotFoundRecipeException, PartialUpdateException 
	{
		Recipe r = recipeRepository.findOne(recipe.getId());
		
		if (r == null) {
			throw new NotFoundRecipeException("The requested recipe was not found");
		}	
		
//		System.out.println("---------------------------------");
		
		for (Recipe rec : recipeRepository.findByGroupId(r.getGroupId()))
		{
			int matchedTriParams = 0;
			int matchedActParams = 0;
			int totTriParams = rec.getTrigger_ingredients().size();
			int totActParams = rec.getAction_ingredients().size();
			
			for (TriggerIngredient ti : rec.getTrigger_ingredients())
			{
				for (ParametersPOJO param : recipe.getTrigger().getParameters())
				{
					if (ti.getParam().getId().equals(param.getId())) 
					{
						matchedTriParams++;
						ti.setValue(param.getValue());
						break;
					}
				}
			}
			
			for (ActionIngredient ai : rec.getAction_ingredients())
			{
				for (ActionPOJO act : recipe.getActions())
				{
					if (rec.getAction().getId().equals(act.getId()))
					{
						for (ParametersPOJO param : act.getParameters())
						{
							if (ai.getParam().getId().equals(param.getId()))
							{
								matchedActParams++;
								ai.setValue(param.getValue());
								break;
							}
						}
						
						break;
					}
				}
			}
			
//			System.out.println("id: " + rec.getId() + " - tot_tri: " + totTriParams + " - match_tri: " + matchedTriParams);
//			System.out.println("id: " + rec.getId() + " - tot_act: " + totActParams + " - match_act: " + matchedActParams);
			
			rec.setDescription(recipe.getDescription());
			
			if (matchedTriParams == 0 && matchedActParams == 0) {
				throw new PartialUpdateException("The update has not been successful because of all unknown parameter ids");
			}
			
			if (matchedTriParams < totTriParams) {
				throw new PartialUpdateException("The update has been partial because of some unknown trigger parameter ids");
			}
			
			if (matchedActParams < totActParams) {
				throw new PartialUpdateException("The update has been partial because of some unknown action parameter ids");
			}
			
			recipeRepository.save(rec);
		}
	}
	
	private void validateRecipe(List<Recipe> recipes) throws AddRecipeException
	{
		//TODO controlli user ???
		System.out.println("ADDING RECIPE, CONTROLLING THE RECIPE LIST");
		//checks on recipes
		for (Recipe r : recipes)
		{
			System.out.println("CONTROLLING A RECIPE");
			if (r.getAction() != null && r.getAction_ingredients() != null && r.getTrigger() != null && r.getTrigger_ingredients() != null && r.getDescription() != null)
			{
				System.out.println("ALL FIELDS ARE DIFFERENT FROM NULL");
				//check if parameters are valid
				boolean are_all_instances = true;
				boolean are_all_valid = true;
				
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
				
					if (are_all_instances == true)
					{
						for (TriggerIngredient ti : r.getTrigger_ingredients())
						{
							if (!(ti instanceof TriggerIngredient))
							{
								are_all_instances = false;
								break;
							}
						}
					}
					
					if (actionService.findById(r.getAction().getId()) != null && triggerService.findById(r.getTrigger().getId()) != null)
					{
						System.out.println("ACTION AND TRIGGER ARE VALID");
						
						//FIXME alla fine mettere il break ed eliminare i messaggi sotto
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
						
						//FIXME alla fine mettere il break ed eliminare i messaggi sotto
						//FIXME inoltre è bene eseguire questo for solo se are_all_valid == true
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
						
						//FIXME sembra che nonostante l'eccezione lanciata, il db viene modificato
						if (!are_all_instances || !are_all_valid)
						{
							System.out.println("Invalid data in recipe sent (2)");
							throw new AddRecipeException("ERROR: Invalid data in recipe sent");
						}
						
						//validate parameters_keyword
						if(r.getParameters_keyword()!=null){
							Map<String, String> map_name_value_real = new HashMap<String, String>();
							Map<String, String> map_name_value_received = new HashMap<String, String>();
							//get all real parameters ingredients
							for(TriggerIngredient pt : r.getTrigger_ingredients()){
								map_name_value_real.put(pt.getParam().getName(), pt.getValue());
							}
							for(ParametersKeyword pk : r.getParameters_keyword()){
								map_name_value_received.put(pk.getName(), pk.getValue());
							}
							if(!map_name_value_real.equals(map_name_value_received)){
								System.out.println("Parameters Keywords does not match trigger data received");
								throw new AddRecipeException("ERROR: Parameters Keywords does not match trigger data received");								
							}
						}
					}
					else
					{
						System.out.println("At least one among trigger and action is not valid");
						throw new AddRecipeException("ERROR: At least one among trigger and action is not valid");
					}
				}
				else
				{
					System.out.println("Invalid data in recipe sent (1)");
					throw new AddRecipeException("ERROR: Invalid data in recipe sent");
				}
			}
			else
			{
				System.out.println("At least one recipe sent misses a field");
				throw new AddRecipeException("ERROR: At least one recipe sent misses a field");
			}
		}
	}
	
	private boolean is_text_type(Object value) {
		return value instanceof String;
	}
	
	private boolean is_number_type(Object value) 
	{
		if (value instanceof String)
		{
			try
			{
				Integer.parseInt((String) value);
				return true;
			}
			catch (NumberFormatException e)
			{
				return false;
			}
		}
		
		return false;
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
			    System.err.println(ex.getMessage());
			    return false;
			}
			
			return (date != null) ? true : false;
		}
		
		return false;
	}
	
	//FIXME bisogna controllare che nel radio 1 e 1 sola opzione sia settata e nel checkbox almeno 1
	private boolean is_radio_type_or_checkbox_valid(Object value, Object ingredient)
	{
		Long param_id;
		String real_name;
		
		if (!(value instanceof String)) return false;
		
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
		
		return (((String) value).equals(real_name) || ((String) value).equals("unchecked_radio_button") || ((String) value).equals("unchecked_checkbox_button")) ? true : false;
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
}