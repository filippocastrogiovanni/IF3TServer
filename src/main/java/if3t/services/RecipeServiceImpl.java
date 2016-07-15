package if3t.services;

import java.util.List;
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
import if3t.models.Channel;
import if3t.models.Recipe;
import if3t.models.User;
import if3t.repositories.AuthorizationRepository;
import if3t.repositories.RecipeRepository;

@Service
@Transactional
public class RecipeServiceImpl implements RecipeService {

	@Autowired
	private RecipeRepository recipeRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private AuthorizationRepository authRepository;
	
	//TODO controllare se quest'annotazione serve anche in altri service
	@PreAuthorize("hasRole('USER')")
	public List<Recipe> readUserRecipes(Long userId) {
		return recipeRepository.findByUser_Id(userId);
	}

	public List<Recipe> readPublicRecipes() {
		return recipeRepository.findByIsPublic(true);
	}

	@PreAuthorize("hasRole('USER')")
	public List<Recipe> readRecipe(Long id, User loggedUser) throws NoPermissionException {
		Recipe targetRecipe = recipeRepository.findOne(id);
		String groupId = targetRecipe.getGroupId();
		
		List<Recipe> recipeList = recipeRepository.findByGroupId(groupId);
		for(Recipe recipe: recipeList){
			if(!recipe.getUser().equals(loggedUser))
				throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		}
		
		return recipeList;
	}

	@PreAuthorize("hasRole('USER')")
	public void deleteRecipe(Long id, User loggedUser) throws NoPermissionException {
		Recipe recipe = recipeRepository.findOne(id);
		if(!recipe.getUser().getId().equals(loggedUser.getId()))
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		recipeRepository.delete(recipe);
	}

	@PreAuthorize("hasRole('USER')")
	public void addRecipe(List<Recipe> recipe) throws NotLoggedInException {
		UUID groupId = UUID.randomUUID();
		for(Recipe r: recipe){
			r.setGroupId(groupId.toString());
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth == null)
				throw new NotLoggedInException("ERROR: not logged in!");
			User loggedUser = userService.getUserByUsername(auth.getName());
			r.setUser(loggedUser);
			recipeRepository.save(r);
		}
	}

	@PreAuthorize("hasRole('USER')")
	public void toggleIsPublicRecipe(Recipe recipe) 
	{
		recipe.setIsPublic(!recipe.getIsPublic());
		recipeRepository.save(recipe);
	}

	@PreAuthorize("hasRole('USER')")
	public void toggleIsEnabledRecipe(Recipe recipe, User user) throws ChannelNotAuthorizedException 
	{		
		Long userId = user.getId();
		Channel triggerChannel = recipe.getTrigger().getChannel();
		Channel actionChannel = recipe.getAction().getChannel();
		
		if (authRepository.findByUser_IdAndChannel_ChannelId(userId, triggerChannel.getChannelId()) == null)
			throw new ChannelNotAuthorizedException("Trigger channel (" + triggerChannel.getName() + ") not authorized!");
		
		if (authRepository.findByUser_IdAndChannel_ChannelId(userId, actionChannel.getChannelId()) == null)
			throw new ChannelNotAuthorizedException("Action channel (" + actionChannel.getName() + ") not authorized!");
		
		recipe.setIsEnabled(!recipe.getIsEnabled());
		recipeRepository.save(recipe);
	}
}