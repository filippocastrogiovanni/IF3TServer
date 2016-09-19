package if3t.repositories;

import org.springframework.data.repository.Repository;

import if3t.entities.ActionIngredient;
import if3t.entities.City;

public interface CityRepository extends Repository<City, Long>{
	
	public Iterable<ActionIngredient> findAll();
	public ActionIngredient findOne(Long id);
}
