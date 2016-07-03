package if3t.services;

import java.util.List;

import if3t.models.Recipe;

public interface RecipeService {

	public List<Recipe> getUserRecipes(String username);
	public List<Recipe> getPublicRecipes();
	public Recipe getRecipe(Long id);
}
