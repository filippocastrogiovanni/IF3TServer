package if3t.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.entities.Authorization;
import if3t.repositories.AuthorizationRepository;
import if3t.repositories.ChannelRepository;

@Service
@Transactional
public class AuthorizationServiceImpl implements AuthorizationService 
{
	@Autowired
	private AuthorizationRepository authRepository;
	
	@Autowired
	private ChannelRepository channelRepository;
	
	@Override
	public Authorization getAuthorization(Long userId, String channelKeyword) {
		return authRepository.findByUser_IdAndChannel_ChannelId(userId, channelRepository.findByKeyword(channelKeyword).getChannelId());
	}
	
	@Override
	public void deleteAuthorization(Long authId) {
		authRepository.deleteById(authId);
	}

	@Override
	public void deleteAuthorization(Long userId, String channelKeyword) {
		authRepository.deleteByUser_IdAndChannel_Keyword(userId, channelKeyword);
	}
}