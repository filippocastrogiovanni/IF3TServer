package if3t.repositories;

import if3t.models.ChannelStatus;
import org.springframework.data.repository.Repository;

public interface ChannelsStatusesRepository extends Repository<ChannelStatus, Long>
{
	public ChannelStatus findByUser_IdAndChannel_Keyword(Long userId, String keyword);
}
