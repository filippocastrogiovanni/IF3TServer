package if3t.services;

import if3t.models.ChannelStatus;

public interface ChannelStatusService {
	public ChannelStatus readChannelStatus(Long userId, String keyword);
	public void updateChannelStatus(Long statusId, Long sinceRef);
	public void updateChannelStatus(ChannelStatus channelStatus);
}
