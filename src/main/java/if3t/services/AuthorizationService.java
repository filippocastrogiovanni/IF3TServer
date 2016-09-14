package if3t.services;

import if3t.models.Authorization;

public interface AuthorizationService 
{
	public Authorization getAuthorization(Long userId, String channelKeyword);
	public void deleteAuthorization(Long authId);
}
