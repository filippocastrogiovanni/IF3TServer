package if3t.repositories;

import if3t.models.ChannelStatus;
import org.springframework.data.repository.Repository;

public interface ChannelsStatusesRepository extends Repository<ChannelStatus, Long>
{
	public ChannelStatus findOne(Long id);
	public ChannelStatus findByRecipe_Id(Long recipeId);
	public ChannelStatus save(ChannelStatus status);
}
