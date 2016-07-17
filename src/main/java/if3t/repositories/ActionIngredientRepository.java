package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;

import if3t.models.ActionIngredient;

public interface ActionIngredientRepository extends Repository<ActionIngredient, Long> {

	public Iterable<ActionIngredient> findAll();
	public ActionIngredient findOne(Long id);
	public List<ActionIngredient> findByRecipe_Id(Long recipeId);
}
