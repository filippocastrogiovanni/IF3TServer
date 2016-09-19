package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;

import if3t.entities.ActionIngredient;

public interface ActionIngredientRepository extends Repository<ActionIngredient, Long> {

	public Iterable<ActionIngredient> findAll();
	public ActionIngredient findOne(Long id);
	public List<ActionIngredient> findByRecipe_Id(Long recipeId);
	public ActionIngredient save(ActionIngredient ai);
	//FIXME forse il parametro per cancellare è l'id
	public void delete(ActionIngredient ai);
}
