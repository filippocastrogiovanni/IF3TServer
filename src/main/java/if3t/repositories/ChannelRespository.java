package if3t.repositories;

import org.springframework.data.repository.Repository;

import if3t.models.Channel;

public interface ChannelRespository extends Repository<Channel, Long> {

	public Iterable<Channel> findAll();
}
