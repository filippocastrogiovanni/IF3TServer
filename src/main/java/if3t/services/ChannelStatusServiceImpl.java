package if3t.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.entities.ChannelStatus;
import if3t.repositories.ChannelsStatusesRepository;
import if3t.repositories.RecipeRepository;

@Service
@Transactional
public class ChannelStatusServiceImpl implements ChannelStatusService
{
	@Autowired
	private ChannelsStatusesRepository channelsStatusesRepo;
	@Autowired
	private RecipeRepository recipeRepo;
	
	@Override
	public ChannelStatus createNewChannelStatus(Long recipeId, Long sinceRef) 
	{
		ChannelStatus cs = new ChannelStatus();
		cs.setRecipe(recipeRepo.findOne(recipeId));
		cs.setSinceRef(sinceRef);
		channelsStatusesRepo.save(cs);
		return cs;
	}
	
	@Override
	public ChannelStatus createNewChannelStatus(Long recipeId, Long sinceRef, Long facebookSinceRef) 
	{
		ChannelStatus cs = new ChannelStatus();
		cs.setRecipe(recipeRepo.findOne(recipeId));
		cs.setSinceRef(sinceRef);
		cs.setFacebookSinceRef(facebookSinceRef);
		channelsStatusesRepo.save(cs);
		return cs;
	}
	
	@Override
	public ChannelStatus createNewChannelStatus(Long recipeId, Long sinceRef, String pageToken) 
	{
		ChannelStatus cs = new ChannelStatus();
		cs.setRecipe(recipeRepo.findOne(recipeId));
		cs.setSinceRef(sinceRef);
		cs.setPageToken(pageToken);
		channelsStatusesRepo.save(cs);
		return cs;
		
	}
	
	@Override
	public ChannelStatus readChannelStatusByRecipeId(Long recipeId) {
		return channelsStatusesRepo.findByRecipe_Id(recipeId);
	}
	
	@Override
	public ChannelStatus updateChannelStatus(Long statusId, Long sinceRef) 
	{
		ChannelStatus status = channelsStatusesRepo.findOne(statusId);
		status.setSinceRef(sinceRef);
		channelsStatusesRepo.save(status);
		return status;
	}

	@Override
	public ChannelStatus updateChannelStatus(ChannelStatus channelStatus) 
	{
		channelsStatusesRepo.save(channelStatus);
		return channelStatus;
	}	
}