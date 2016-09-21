package if3t.services;

import if3t.entities.ChannelStatus;

public interface ChannelStatusService 
{
	public ChannelStatus createNewChannelStatus(Long recipeId, Long sinceRef);
	public ChannelStatus readChannelStatusByRecipeId(Long recipeId);
	public ChannelStatus updateChannelStatus(Long statusId, Long sinceRef);
	public ChannelStatus updateChannelStatus(ChannelStatus channelStatus);
}
