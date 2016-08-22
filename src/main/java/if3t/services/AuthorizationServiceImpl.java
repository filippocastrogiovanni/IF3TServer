package if3t.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.models.Authorization;
import if3t.repositories.AuthorizationRepository;

@Service
@Transactional
public class AuthorizationServiceImpl implements AuthorizationService {

	@Autowired
	private AuthorizationRepository authRepository;
	
	public Authorization getAuthorization(Long userId, Long channelId) {
		return authRepository.findByUser_IdAndChannel_ChannelId(userId, channelId);
	}
	
	public void deleteAuthorization(Long authId) {
		authRepository.deleteById(authId);
	}

	@Override
	public Authorization findByUser_IdAndChannel_ChannelId(Long id, Long channelId) {
		authRepository.queryByUser_IdAndChannel_ChannelId(id, channelId);
		return null;
	}
		

}