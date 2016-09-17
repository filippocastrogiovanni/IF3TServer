package if3t.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.models.ChannelStatus;
import if3t.repositories.ChannelsStatusesRepository;

@Service
@Transactional
public class ChannelStatusServiceImpl implements ChannelStatusService
{
	@Autowired
	private ChannelsStatusesRepository channelsStatusesRepo;
	
	@Override
	public ChannelStatus readChannelStatusByRecipeId(Long recipeId) 
	{
		return channelsStatusesRepo.findByRecipe_Id(recipeId);
	}

	@Override
	public void updateChannelStatus(Long statusId, Long sinceRef) 
	{
		ChannelStatus status = channelsStatusesRepo.findOne(statusId);
		status.setSinceRef(sinceRef);
		channelsStatusesRepo.save(status);
	}

	@Override
	public void updateChannelStatus(ChannelStatus channelStatus) {
		channelsStatusesRepo.save(channelStatus);
	}
}
