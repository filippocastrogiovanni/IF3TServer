package if3t.services;

import if3t.entities.ChannelStatus;

public interface ChannelStatusService 
{
	public void createNewChannelStatus(Long recipeId, Long sinceRef);
	public void createNewChannelStatus(Long recipeId, Long sinceRef, Long facebookSinceRef);
	public ChannelStatus readChannelStatusByRecipeId(Long recipeId);
	public void updateChannelStatus(Long statusId, Long sinceRef);
	public void updateChannelStatus(ChannelStatus channelStatus);
}
