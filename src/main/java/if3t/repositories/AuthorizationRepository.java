package if3t.repositories;

import org.springframework.data.repository.Repository;

import if3t.models.Authorization;

public interface AuthorizationRepository extends Repository<Authorization, Long> {

	public Authorization findOne(Long id);
	public Authorization deleteByUser_IdAndChannel_ChannelId(Long userId, Long channelId);
	public Authorization save(Authorization auth);
	public Authorization findByUser_IdAndChannel_ChannelId(Long userId, Long channelId);
}
