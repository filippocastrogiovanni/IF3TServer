package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;

import if3t.models.Action;
import if3t.models.Channel;
import if3t.models.Timezone;
import if3t.models.User;

public interface TimezoneRepository extends Repository<Timezone, Long> {

	public Iterable<Timezone> findAll();
	public Timezone findOne(Long id);
	
}
