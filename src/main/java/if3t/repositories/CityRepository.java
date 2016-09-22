package if3t.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import if3t.entities.City;

public interface CityRepository extends Repository<City, Long>
{	
	public List<City> findAll();
	public City findOne(Long id);
	@Query("SELECT c FROM City c WHERE c.name LIKE CONCAT('%',:keyword,'%')")
	public List<City> findCitiesWithPartOfName(@Param("keyword") String keyword, Pageable pageable);
}