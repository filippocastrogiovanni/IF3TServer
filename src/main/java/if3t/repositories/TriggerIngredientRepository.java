package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;

import if3t.models.TriggerIngredient;

public interface TriggerIngredientRepository extends Repository<TriggerIngredient, Long> {

	public Iterable<TriggerIngredient> findAll();
	public TriggerIngredient findOne(Long id);
	public List<TriggerIngredient> findByRecipe_Id(Long recipeId);
}
