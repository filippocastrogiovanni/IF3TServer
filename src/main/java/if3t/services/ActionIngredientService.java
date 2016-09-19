package if3t.services;

import java.util.List;
import java.util.Map;

import if3t.entities.ActionIngredient;

public interface ActionIngredientService 
{	
	public ActionIngredient readActionIngredient(Long id);
	public List<ActionIngredient> readActionIngredients();
	public List<ActionIngredient> getRecipeActionIngredients(Long recipeId);
	public Map<Long, ActionIngredient> getRecipeActionIngredientsMap(Long recipeId);
}
