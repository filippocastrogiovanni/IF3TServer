package if3t.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.NoPermissionException;
import if3t.exceptions.NotLoggedInException;
import if3t.models.ActionIngredient;
import if3t.models.Channel;
import if3t.models.Recipe;
import if3t.models.TriggerIngredient;
import if3t.models.User;
import if3t.repositories.ActionIngredientRepository;
import if3t.repositories.AuthorizationRepository;
import if3t.repositories.RecipeRepository;
import if3t.repositories.TriggerIngredientRepository;

@Service
@Transactional
public class RecipeServiceImpl implements RecipeService {

	@Autowired
	private RecipeRepository recipeRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private AuthorizationRepository authRepository;
	@Autowired
	private TriggerIngredientRepository triggerIngRepo;
	@Autowired
	private ActionIngredientRepository actionIngRepo;
	
	//TODO controllare se quest'annotazione serve anche in altri services
	@PreAuthorize("hasRole('USER')")
	public List<Recipe> readUserRecipes(Long userId) {
		return recipeRepository.findByUser_Id(userId);
	}

	public List<Recipe> readPublicRecipes() {
		return recipeRepository.findByIsPublic(true);
	}

	@PreAuthorize("hasRole('USER')")
	public List<Recipe> readRecipe(Long id, User loggedUser) throws NoPermissionException 
	{
		String groupId = recipeRepository.findOne(id).getGroupId();
		List<Recipe> recipeList = recipeRepository.findByGroupId(groupId);
		
		for (Recipe recipe: recipeList)
		{
			if (!recipe.getUser().equals(loggedUser)) {
				throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
			}
		}
		
		return recipeList;
	}

	@PreAuthorize("hasRole('USER')")
	public void deleteRecipe(Long id, User loggedUser) throws NoPermissionException 
	{
		Recipe recipe = recipeRepository.findOne(id);
		
		if (!recipe.getUser().getId().equals(loggedUser.getId())) {
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		}
		
		for (Recipe rec : recipeRepository.findByGroupId(recipe.getGroupId()))
		{
			recipeRepository.delete(rec);
		}
	}

	@PreAuthorize("hasRole('USER')")
	public void addRecipe(List<Recipe> recipe) throws NotLoggedInException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not logged in!");
		}
		
		UUID groupId = UUID.randomUUID();
		User loggedUser = userService.getUserByUsername(auth.getName());
		
		for (Recipe r : recipe)
		{
			r.setIsEnabled(false);
			r.setIsPublic(false);
			r.setGroupId(groupId.toString());	
			r.setUser(loggedUser);
			recipeRepository.save(r);
			
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
}