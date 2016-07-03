package if3t.repositories;

import java.util.List;
import org.springframework.data.repository.Repository;
import if3t.models.Channel;
import if3t.models.Trigger;

public interface TriggerRepository extends Repository<Trigger, Long> {

	public Iterable<Trigger> findAll();
	public List<Trigger> findByChannel(Channel channel);
	public Trigger findOne(Long id);
}
