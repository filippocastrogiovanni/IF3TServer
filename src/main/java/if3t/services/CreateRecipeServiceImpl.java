package if3t.services;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import if3t.models.Action;
import if3t.models.Channel;
import if3t.models.Parameter;
import if3t.models.Trigger;
import if3t.repositories.ActionRepository;
import if3t.repositories.ChannelRespository;
import if3t.repositories.ParamRepository;
import if3t.repositories.TriggerRepository;

@Service
@Transactional
public class CreateRecipeServiceImpl implements CreateRecipeService {

	@Autowired
	private ChannelRespository channelRepository;
	@Autowired
	private ActionRepository actionRepository;
	@Autowired
	private TriggerRepository triggerRepository;
	@Autowired
	private ParamRepository paramRepository;
	
	public List<Channel> readChannels() {
		List<Channel> channels = new ArrayList<Channel>();
		
		for(Channel channel: channelRepository.findAll())
			channels.add(channel);
		
		return channels;
	}
	
	public Channel readChannel(Long id) {
		return channelRepository.findOne(id);
	}

	public List<Action> readChannelActions(Long channelId) {
		Channel channel = channelRepository.findOne(channelId);
		return actionRepository.findByChannel(channel);
	}

	public Action readAction(Long id) {
		return actionRepository.findOne(id);
	}

	public List<Trigger> readChannelTriggers(Long channelId) {
		Channel channel = channelRepository.findOne(channelId);
		return triggerRepository.findByChannel(channel);
	}

	public Trigger readTrigger(Long id) {
		return triggerRepository.findOne(id);
	}

	public List<Parameter> readChannelParameters(Long channelId) {
		Channel channel = channelRepository.findOne(channelId);
		return paramRepository.findByChannel(channel);
	}

	public Parameter readParameter(Long id) {
		return paramRepository.findOne(id);
	}

}
