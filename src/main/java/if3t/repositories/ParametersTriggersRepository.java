package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;
import if3t.models.Channel;
import if3t.models.ParametersTriggers;
import if3t.models.Trigger;

public interface ParametersTriggersRepository extends Repository<ParametersTriggers, Long> {

	public Iterable<ParametersTriggers> findAll();
	public List<ParametersTriggers> findByChannel(Channel channel);
	public ParametersTriggers findOne(Long id);
	public List<ParametersTriggers> findByChannelAndType(Channel channel, Byte type);
	public List<ParametersTriggers> findByTriggerAndChannel(Trigger trigger, Channel channel);
}