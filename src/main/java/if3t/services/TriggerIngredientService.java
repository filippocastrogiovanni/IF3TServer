package if3t.services;

import java.util.List;

import if3t.models.TriggerIngredient;

public interface TriggerIngredientService {
	
	public TriggerIngredient readTriggerIngredient(Long id);
	public List<TriggerIngredient> readTriggerIngredients();
	public List<TriggerIngredient> getRecipeTriggerIngredients(Long recipeId);
}
