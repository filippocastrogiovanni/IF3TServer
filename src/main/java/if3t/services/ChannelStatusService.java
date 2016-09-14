package if3t.services;

import if3t.models.ChannelStatus;

public interface ChannelStatusService 
{
	public ChannelStatus readChannelStatus(Long userId, String keyword);
}
