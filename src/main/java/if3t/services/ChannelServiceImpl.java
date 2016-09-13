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
public class ChannelServiceImpl implements ChannelService 
{
	@Autowired
	private ChannelRepository channelRepository;
	@Autowired
	private AuthorizationRepository authRepository;
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public List<Channel> readChannels() 
	{
		List<Channel> channels = new ArrayList<Channel>();
		
		for (Channel channel: channelRepository.findAll())
		{
			channels.add(channel);
		}
		
		return channels;
	}
	
	@Override
	public Channel readChannel(Long id) {
		return channelRepository.findOne(id);
	}

	@Override
	public List<Channel> readUserChannels(Long userId) {
		return channelRepository.findByAuthorizations_User_Id(userId);
	}

	@Override
	public void unautorizeChannel(Long userId, Long channelId) {
		authRepository.deleteByUser_IdAndChannel_ChannelId(userId, channelId);
	}

	//TODO forse serve considerare il caso in cui l'attuale token è invalido. Allo stato attuale non verrebbe sovrascritto dal nuovo
	@Override
	public void authorizeChannel(Long userId, String channel, String access_token, String refresh_token, String token_type, Long expires_date)
	{
		Channel chan = channelRepository.findByKeyword(channel);
		
		if (authRepository.findByUser_IdAndChannel_ChannelId(userId, chan.getChannelId()) == null)
		{
			Authorization auth = new Authorization();
			auth.setChannel(chan);
			auth.setUser(userRepository.findOne(userId));
			auth.setAccessToken(access_token);
			auth.setRefreshToken(refresh_token);
			auth.setTokenType(token_type);
			auth.setExpireDate(expires_date);
			authRepository.save(auth);
		}
	}
	
	@Override
	public void refreshChannelAuthorization(Authorization auth) {
		authRepository.save(auth);
	}
	
	@Override
	public List<Authorization> readExpiringAuthorizations(String channel, Long timestamp) {
		
		if (timestamp > 0) {
			return authRepository.queryByExpireDateLessThanAndChannel_Keyword(timestamp, channel);
		}
		
		return null;
	}
	
//	@Override
//	public Authorization getChannelAuthorization(Long userId, String channelKey) 
//	{
//		Channel channel = channelRepository.findByKeyword(channelKey);
//		return authRepository.findByUser_IdAndChannel_ChannelId(userId, channel.getChannelId());
//	}
	
	@Override
	public Channel findByKeyword(String keyword) {
		return channelRepository.findByKeyword(keyword);
	}
}
