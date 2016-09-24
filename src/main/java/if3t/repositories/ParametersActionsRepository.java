package if3t.repositories;

import java.util.List;
import org.springframework.data.repository.Repository;
import if3t.entities.Action;
import if3t.entities.Channel;
import if3t.entities.ParametersActions;

public interface ParametersActionsRepository extends Repository<ParametersActions, Long> 
{
	public Iterable<ParametersActions> findAll();
	public List<ParametersActions> findByChannel(Channel channel);
	public ParametersActions findOne(Long id);
	public List<ParametersActions> findByChannelAndType(Channel channel, Byte type);
	public List<ParametersActions> findByActionAndChannel(Action action, Channel channel);
	public List<ParametersActions> findByChannel_channelId(Long channelId);
}