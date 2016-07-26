package if3t.services;

import java.util.List;
import java.util.Map;

import if3t.models.TriggerIngredient;

public interface TriggerIngredientService 
{
	public TriggerIngredient readTriggerIngredient(Long id);
	public List<TriggerIngredient> readTriggerIngredients();
	public List<TriggerIngredient> getRecipeTriggerIngredients(Long recipeId);
	public Map<Long, TriggerIngredient> getRecipeTriggerIngredientsMap(Long recipeId);
}