package if3t.services;

import java.util.List;
import java.util.Set;

import if3t.entities.Authorization;
import if3t.entities.Channel;

public interface ChannelService 
{
	public List<Channel> readChannels();
	public Set<Channel> getChannelsForTrigger();
	public Set<Channel> getChannelsForAction();
	public Channel readChannel(Long id);
	public Channel findByKeyword(String keyword);
	public List<Channel> readUserChannels(Long userId);
	public void unautorizeChannel(Long userId, Long channelId);
	public void authorizeChannel(Long userId, String channel, String access_token, String refresh_token, String token_type, Long expires_date);
	public List<Authorization> readExpiringAuthorizations(String channel, Long timestamp);
	public void refreshChannelAuthorization(Authorization auth);
//	public Authorization getChannelAuthorization(Long userId, String channelKey);
}
