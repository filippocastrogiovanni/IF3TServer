package if3t.services;

import java.util.List;

import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.NoPermissionException;
import if3t.exceptions.NotLoggedInException;
import if3t.models.Recipe;
import if3t.models.User;

public interface RecipeService {

	public List<Recipe> readUserRecipes(Long userId);
	public List<Recipe> readPublicRecipes();
	public List<Recipe> readRecipe(Long id, User loggedUser) throws NoPermissionException;
	public List<Recipe> getRecipeByTriggerChannel(String channelKeyword);
	public void deleteRecipe(Long id, User loggedUser) throws NoPermissionException;
	public void addRecipe(List<Recipe> recipe) throws NotLoggedInException;
	public void toggleIsPublicRecipe(Recipe recipe);
	public void toggleIsEnabledRecipe(Recipe recipe, User user) throws ChannelNotAuthorizedException;
}
