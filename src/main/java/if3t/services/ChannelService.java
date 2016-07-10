package if3t.services;

import java.util.List;
import if3t.models.Channel;

public interface ChannelService {

	public List<Channel> readChannels();
	public Channel readChannel(Long id);
	public List<Channel> readUserChannels(Long userId);
	public void unautorizeChannel(Long userId, Long channelId);
}
