package if3t.services;

import java.util.List;

import if3t.models.ActionIngredient;

public interface ActionIngredientService {
	
	public ActionIngredient readActionIngredient(Long id);
	public List<ActionIngredient> readActionIngredients();
	public List<ActionIngredient> getRecipeActionIngredients(Long recipeId);
}
