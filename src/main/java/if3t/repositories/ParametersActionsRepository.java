package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;
import if3t.models.Channel;
import if3t.models.ParametersActions;

public interface ParametersActionsRepository extends Repository<ParametersActions, Long> {

	public Iterable<ParametersActions> findAll();
	public List<ParametersActions> findByChannel(Channel channel);
	public ParametersActions findOne(Long id);
	public List<ParametersActions> findByChannelAndType(Channel channel, Byte type);
}