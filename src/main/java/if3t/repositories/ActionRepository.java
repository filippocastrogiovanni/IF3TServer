package if3t.repositories;

import org.springframework.data.repository.Repository;

import if3t.models.Action;

public interface ActionRepository extends Repository<Action, Long> {

	public Iterable<Action> findAll();
}
