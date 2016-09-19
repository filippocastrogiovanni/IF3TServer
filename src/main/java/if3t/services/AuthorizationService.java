package if3t.services;

import if3t.entities.Authorization;

public interface AuthorizationService 
{
	public Authorization getAuthorization(Long userId, String channelKeyword);
	public void deleteAuthorization(Long authId);
	public void deleteAuthorization(Long userId, String channelKeyword);
}
