package if3t.repositories;

import org.springframework.data.repository.Repository;
import if3t.models.Timezone;

public interface TimezoneRepository extends Repository<Timezone, Long> {

	public Iterable<Timezone> findAll();
	public Timezone findOne(Long id);
	
}
