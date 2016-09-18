package if3t.services;

import java.util.List;

import if3t.exceptions.AddRecipeException;
import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.NoPermissionException;
import if3t.exceptions.NotFoundRecipeException;
import if3t.exceptions.PartialUpdateException;
import if3t.models.Recipe;
import if3t.models.RecipePOJO;
import if3t.models.User;

public interface RecipeService {

	public List<Recipe> readUserRecipes(Long userId);
	public List<Recipe> readPublicRecipes();
	public List<Recipe> readRecipe(Long id, User loggedUser) throws NoPermissionException, NotFoundRecipeException;
	public List<Recipe> getEnabledRecipesByTriggerChannel(String channelKeyword);
	public void deleteRecipe(Long id, User loggedUser) throws NoPermissionException, NotFoundRecipeException;
	public void addRecipe(List<Recipe> recipe, User loggedUser) throws AddRecipeException;
	public void toggleIsPublicRecipe(Recipe recipe);
	public void toggleIsEnabledRecipe(List<Recipe> recipes, User user) throws ChannelNotAuthorizedException;
	public void updateRecipe(RecipePOJO recipe) throws NotFoundRecipeException, PartialUpdateException;
}