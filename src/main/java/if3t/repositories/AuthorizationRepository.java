package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;

import if3t.models.Authorization;
import if3t.models.Channel;
import if3t.models.User;

public interface AuthorizationRepository extends Repository<Authorization, Long> {

	public Authorization findOne(Long id);
	public void deleteByUser_IdAndChannel_ChannelId(Long userId, Long channelId);
	public void deleteByUser_IdAndChannel_Keyword(Long userId, String channelKeyword);
	public void deleteById(Long authId);
	public Authorization save(Authorization auth);
	public Authorization findByUser_IdAndChannel_ChannelId(Long userId, Long channelId);
	public Authorization findByUserAndChannel(User user, Channel channel);
	public Authorization findByAccessToken(String access_token);
	public Authorization queryByUser_IdAndChannel_ChannelId(Long userId, Long channelId);
	public List<Authorization> queryByExpireDateLessThanAndChannel_Keyword(Long timestamp, String channel);
}
