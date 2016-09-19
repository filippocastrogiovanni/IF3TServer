package if3t.repositories;

import org.springframework.data.repository.Repository;

import if3t.entities.ChannelStatus;

public interface ChannelsStatusesRepository extends Repository<ChannelStatus, Long>
{
	public ChannelStatus findOne(Long id);
	public ChannelStatus findByRecipe_Id(Long recipeId);
	public ChannelStatus save(ChannelStatus status);
}
