package if3t.services;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.models.Authorization;
import if3t.models.Channel;
import if3t.repositories.AuthorizationRepository;
import if3t.repositories.ChannelRepository;
import if3t.repositories.UserRepository;

@Service
@Transactional
public class ChannelServiceImpl implements ChannelService {

	@Autowired
	private ChannelRepository channelRepository;
	@Autowired
	private AuthorizationRepository authRepository;
	@Autowired
	private UserRepository userRepository;
	
	public List<Channel> readChannels() {
		List<Channel> channels = new ArrayList<Channel>();
		
		for(Channel channel: channelRepository.findAll())
			channels.add(channel);
		
		return channels;
	}
	
	public Channel readChannel(Long id) {
		return channelRepository.findOne(id);
	}

	public List<Channel> readUserChannels(Long userId) {
		return channelRepository.findByAuthorizations_User_Id(userId);
	}

	public void unautorizeChannel(Long userId, Long channelId) {
		authRepository.deleteByUser_IdAndChannel_ChannelId(userId, channelId);
	}

	public void authorizeChannel(Long userId, String channel, 
			String access_token, 
			String refresh_token,
			String token_type,
			Long expires_date){
		try {
		Authorization auth = new Authorization();
		auth.setChannel(channelRepository.findByKeyword(channel));
		auth.setUser(userRepository.findOne(userId));
		auth.setAccessToken(access_token);
		auth.setRefreshToken(refresh_token);
		auth.setTokenType(token_type);
		auth.setExpireDate(expires_date);
		authRepository.save(auth);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
