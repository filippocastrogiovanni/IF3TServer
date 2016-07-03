package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;
import if3t.models.Channel;
import if3t.models.Parameter;

public interface ParamRepository extends Repository<Parameter, Long> {

	public Iterable<Parameter> findAll();
	public List<Parameter> findByChannel(Channel channel);
	public Parameter findOne(Long id);
	public List<Parameter> findByChannelAndType(Channel channel, Byte type);
}