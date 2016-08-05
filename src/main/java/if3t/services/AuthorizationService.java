package if3t.services;

import if3t.models.Authorization;

public interface AuthorizationService {

	public Authorization getAuthorization(Long userId, Long channelId);
	public void deleteAuthorization(Long authId);
	public Authorization findByUser_IdAndChannel_ChannelId(Long id, Long channelId);
}
