package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;

import if3t.models.TriggerIngredient;

public interface TriggerIngredientRepository extends Repository<TriggerIngredient, Long> 
{
	public Iterable<TriggerIngredient> findAll();
	public TriggerIngredient findOne(Long id);
	public List<TriggerIngredient> findByRecipe_Id(Long recipeId);
	public TriggerIngredient save(TriggerIngredient ti);
	//FIXME forse il parametro per cancellare è l'id
	public void delete(TriggerIngredient ti);
}
