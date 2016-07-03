package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;

import if3t.models.Action;
import if3t.models.Channel;

public interface ActionRepository extends Repository<Action, Long> {

	public Iterable<Action> findAll();
	public List<Action> findByChannel(Channel channel);
	public Action findOne(Long id);
}
