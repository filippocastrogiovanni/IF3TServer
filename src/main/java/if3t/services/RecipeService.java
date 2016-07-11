package if3t.services;

import java.util.List;

import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.NotLoggedInException;
import if3t.models.Recipe;

public interface RecipeService {

	public List<Recipe> readUserRecipes(Long userId);
	public List<Recipe> readPublicRecipes();
	public List<Recipe> readRecipe(Long id);
	public void deleteRecipe(Long id);
	public void addRecipe(List<Recipe> recipe) throws NotLoggedInException;
	public void publishRecipe(Recipe recipe);
	public void enableRecipe(Recipe recipe) throws NotLoggedInException, ChannelNotAuthorizedException;
}
