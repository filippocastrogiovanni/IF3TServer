package if3t.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import if3t.entities.Action;
import if3t.entities.ActionIngredient;
import if3t.entities.Channel;
import if3t.entities.ParametersActions;
import if3t.entities.ParametersTriggers;
import if3t.entities.Recipe;
import if3t.entities.Trigger;
import if3t.entities.TriggerIngredient;
import if3t.entities.User;
import if3t.exceptions.AddRecipeException;
import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.NoPermissionException;
import if3t.exceptions.NotFoundRecipeException;
import if3t.exceptions.PartialUpdateException;
import if3t.models.ActionPOJO;
import if3t.models.ParametersPOJO;
import if3t.models.RecipePOJO;
import if3t.models.TriggerPOJO;
import if3t.repositories.ActionIngredientRepository;
import if3t.repositories.ActionRepository;
import if3t.repositories.AuthorizationRepository;
import if3t.repositories.ChannelsStatusesRepository;
import if3t.repositories.RecipeRepository;
import if3t.repositories.TriggerIngredientRepository;
import if3t.repositories.TriggerRepository;

@Service
@Transactional
public class RecipeServiceImpl implements RecipeService 
{
	@Autowired
	private RecipeRepository recipeRepository;
	@Autowired
	private AuthorizationRepository authRepository;
	@Autowired
	private TriggerIngredientRepository triggerIngRepo;
	@Autowired
	private ActionIngredientRepository actionIngRepo;
	@Autowired
	private ActionRepository actionRespository;	
	@Autowired
	private TriggerRepository triggerRepository;
	@Autowired
	private CreateRecipeService createRecipeService;
	@Autowired
	private ChannelsStatusesRepository channelStatusRepo;
	@Autowired
	private TriggerIngredientService triggerIngrService;
	@Autowired
	private ActionIngredientService actionIngrService;
	private static final String EMAIL_PATTERN = "^[-a-z0-9~!$%^&*_=+}{\'?]+(.[-a-z0-9~!$%^&*_=+}{\'?]+)*@([a-z0-9_][-a-z0-9_]*(.[-a-z0-9_]+)*.(aero|arpa|biz|com|coop|edu|gov|info|int|mil|museum|name|net|org|pro|travel|mobi|[a-z][a-z])|([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}))(:[0-9]{1,5})?$";
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
	
	@Override
	@PreAuthorize("hasRole('USER')")
	public List<Recipe> readUserRecipes(Long userId) {
		return recipeRepository.findByUser_Id(userId);
	}

	public List<Recipe> readPublicRecipes() {
		return recipeRepository.findByIsPublic(true);
	}

	@Override
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
			if (!recipe.getUser().equals(loggedUser)) 
			{
				logger.error("The user " + loggedUser.getUsername() + " doesn't have permissions to read the recipe!");
				throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
			}
		}
		
		return recipeList;
	}

	@Override
	@PreAuthorize("hasRole('USER')")
	public void deleteRecipe(Long id, User loggedUser) throws NoPermissionException, NotFoundRecipeException 
	{
		Recipe recipe = recipeRepository.findOne(id);
		
		if (recipe == null) {
			throw new NotFoundRecipeException("The requested recipe was not found");
		}		
		
		if (!recipe.getUser().getId().equals(loggedUser.getId())) 
		{
			logger.error("The user " + loggedUser.getUsername() + " doesn't have permissions to delete the recipe!");
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
			
			channelStatusRepo.deleteByRecipe_id(rec.getId());
			recipeRepository.delete(rec);
		}
	}

	@Override
	@PreAuthorize("hasRole('USER')")
	public void addRecipe(List<Recipe> recipes, User loggedUser) throws AddRecipeException
	{		
//		System.out.println("Saving recipe");
		validateRecipe(recipes);
		
		for (Recipe r : recipes)
		{
			r.setIsEnabled(true);
			r.setIsPublic(false);
			r.setGroupId(UUID.randomUUID().toString());	
			r.setUser(loggedUser);
			
//			System.out.println("Recipe ready to save");
			recipeRepository.save(r);
//			System.out.println("Recipe saved");

			/*if(r.getParameters_keyword()!=null)
				for(ParametersKeyword pk : r.getParameters_keyword()){
					pk.setRecipe(r);
					parameterKeywordRepository.save(pk);
				}*/
			
			for (TriggerIngredient ti : r.getTrigger_ingredients())
			{
				ti.setRecipe(r);
				triggerIngRepo.save(ti);
			}
			
//			System.out.println("Trigger ingredients saved");
			
			for (ActionIngredient ai : r.getAction_ingredients())
			{
				ai.setRecipe(r);
				actionIngRepo.save(ai);
			}
			
//			System.out.println("Action ingredient saved");
		}
	}

	@Override
	@PreAuthorize("hasRole('USER')")
	public void toggleIsPublicRecipe(Recipe recipe) 
	{
		recipe.setIsPublic(!recipe.getIsPublic());
		recipeRepository.save(recipe);
	}

	@Override
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
		
		for (Recipe rec : recipes)
		{
			rec.setIsEnabled(!rec.getIsEnabled());
			recipeRepository.save(rec);
		}
	}

	@Override
	public List<Recipe> getEnabledRecipesByTriggerChannel(String channelKeyword) {
		return recipeRepository.findByIsEnabledAndTrigger_Channel_Keyword(true, channelKeyword);
	}
	
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
			
			channelStatusRepo.deleteByRecipe_id(rec.getId());
			recipeRepository.save(rec);
		}
	}
	
	private void validateRecipe(List<Recipe> recipes) throws AddRecipeException
	{
//		System.out.println("ADDING RECIPE, CONTROLLING THE RECIPE LIST");
		//checks on recipes
		for (Recipe r : recipes)
		{
//			System.out.println("CONTROLLING A RECIPE");
			if (r.getAction() != null && r.getAction_ingredients() != null && r.getTrigger() != null && r.getTrigger_ingredients() != null && r.getDescription() != null)
			{
//				System.out.println("ALL FIELDS ARE DIFFERENT FROM NULL");
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
					
					if (actionRespository.findOne(r.getAction().getId()) != null && triggerRepository.findOne(r.getTrigger().getId()) != null)
					{
//						System.out.println("ACTION AND TRIGGER ARE VALID");
						
						for (TriggerIngredient ti : r.getTrigger_ingredients())
						{
							if (!validate_ingredient(ti)) 
							{
								are_all_valid = false;
								logger.error("Invalid ingredient: " + ti);
								break;
							}
							
//							if (are_all_valid) {
//								System.out.println("This trigger ingredient is valid");
//							}
//							else {
//								System.err.println(ti);
//								System.err.println("This trigger ingredient is not valid");
//							}
						}
						
						if (are_all_valid == true)
						{
							for (ActionIngredient ai : r.getAction_ingredients())
							{
								if (!validate_ingredient(ai)) 
								{
									are_all_valid = false;
									logger.error("Invalid ingredient: " + ai);
									break;
								}
								
//								if (are_all_valid) {
//									System.out.println("This action ingredient is valid");
//								}
//								else {
//									System.err.println(ai);
//									System.err.println("This action ingredient is not valid");
//								}
							}
						}
						
						if (!are_all_instances || !are_all_valid)
						{
//							System.out.println("Invalid data in recipe sent (2)");
							logger.error("Invalid data in recipe sent (2)");
							throw new AddRecipeException("ERROR: Invalid data in recipe sent");
						}
						
						/*//validate parameters_keyword
						if(r.getParameters_keyword()!=null && r.getParameters_keyword().size()!=0){
							HashSet<String> hashset_name_real = new HashSet<String>();
							HashSet<String> hashset_name_received = new HashSet<String>();
							//get all real parameters ingredients
							for(TriggerIngredient pt : r.getTrigger_ingredients()){
								hashset_name_real.add(pt.getParam().getName());
							}
							for(ParametersKeyword pk : r.getParameters_keyword()){
								hashset_name_received.add(pk.getName());
							}
							if(!hashset_name_real.equals(hashset_name_received)){
								System.out.println("Parameters Keywords does not match trigger data received");
								throw new AddRecipeException("ERROR: Parameters Keywords does not match trigger data received");								
							}
						}*/
					}
					else
					{
//						System.out.println("At least one among trigger and action is not valid");
						logger.error("At least one among trigger and action is not valid");
						throw new AddRecipeException("ERROR: At least one among trigger and action is not valid");
					}
				}
				else
				{
//					System.out.println("Invalid data in recipe sent (1)");
					logger.error("Invalid data in recipe sent (1)");
					throw new AddRecipeException("ERROR: Invalid data in recipe sent");
				}
			}
			else
			{
//				System.out.println("At least one recipe sent misses a field");
				logger.error("At least one recipe sent misses a field");
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

	@Override
	public RecipePOJO readRecipePOJO(Long id, User loggedUser) throws NoPermissionException, NotFoundRecipeException
	{
		List<Recipe> recList = readRecipe(id, loggedUser);
		Trigger trig = recList.get(0).getTrigger();
		List<ParametersTriggers> ptList = createRecipeService.readChannelParametersTriggers(trig.getId(), trig.getChannel().getChannelId());
		Map<Long, TriggerIngredient> tiMap = triggerIngrService.getRecipeTriggerIngredientsMap(recList.get(0).getGroupId());
		TriggerPOJO trigPOJO = new TriggerPOJO(trig, ptList, tiMap);
		List<ActionPOJO> actPOJOList = new ArrayList<ActionPOJO>();
		
		for (Recipe rec : readRecipe(id, loggedUser))
		{
			Action act = rec.getAction();
			List<ParametersActions> paList = createRecipeService.readChannelParametersActions(act.getId(), act.getChannel().getChannelId());
			Map<Long, ActionIngredient> aiMap = actionIngrService.getRecipeActionIngredientsMap(rec.getId());
			actPOJOList.add(new ActionPOJO(act, paList, aiMap));
		}		
		
		return new RecipePOJO(readRecipe(id, loggedUser), trigPOJO, actPOJOList);
	}
}