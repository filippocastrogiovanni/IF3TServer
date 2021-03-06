package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;

import if3t.entities.Channel;

public interface ChannelRepository extends Repository<Channel, Long> 
{
	public Iterable<Channel> findAll();
	public Channel findOne(Long id);
	public List<Channel> findByAuthorizations_User_Id(Long userId);
	public Channel findByKeyword(String keyword);
}
