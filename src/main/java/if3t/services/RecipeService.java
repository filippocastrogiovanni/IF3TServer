package if3t.services;

import java.util.List;

import if3t.models.Recipe;

public interface RecipeService {

	public List<Recipe> readUserRecipes(String username);
	public List<Recipe> readPublicRecipes();
	public List<Recipe> readRecipe(Long id);
	public void deleteRecipe(Long id);
	public void addRecipe(List<Recipe> recipe);
	public void updateRecipe(Recipe recipe);
}
