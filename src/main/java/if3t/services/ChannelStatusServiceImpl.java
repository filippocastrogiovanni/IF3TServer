package if3t.services;

import org.springframework.beans.factory.annotation.Autowired;

import if3t.models.ChannelStatus;
import if3t.repositories.ChannelsStatusesRepository;

public class ChannelStatusServiceImpl implements ChannelStatusService
{
	@Autowired
	private ChannelsStatusesRepository channelsStatusesRepo;
	
	@Override
	public ChannelStatus readChannelStatus(Long userId, String keyword) 
	{
		return channelsStatusesRepo.findByUser_IdAndChannel_Keyword(userId, keyword);
	}
}
