package if3t.repositories;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import if3t.entities.ActionIngredient;
import if3t.entities.City;

public interface CityRepository extends Repository<City, Long>{
	
	public Collection<ActionIngredient> findAll();
	public ActionIngredient findOne(Long id);
	@Query("SELECT c FROM City c WHERE c.name LIKE CONCAT('%',:keyword,'%')")
	public Collection<City> findCitiesWithPartOfName(@Param("keyword") String keyword);
}
