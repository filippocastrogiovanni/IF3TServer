package if3t.services;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.entities.Action;
import if3t.entities.Channel;
import if3t.entities.ParametersActions;
import if3t.entities.ParametersTriggers;
import if3t.entities.Trigger;
import if3t.repositories.ActionRepository;
import if3t.repositories.ChannelRepository;
import if3t.repositories.ParametersActionsRepository;
import if3t.repositories.ParametersTriggersRepository;
import if3t.repositories.TriggerRepository;

@Service
@Transactional
public class CreateRecipeServiceImpl implements CreateRecipeService 
{
	@Autowired
	private ChannelRepository channelRepository;
	@Autowired
	private ActionRepository actionRepository;
	@Autowired
	private TriggerRepository triggerRepository;
	@Autowired
	private ParametersTriggersRepository parametersTriggersRepository;
	@Autowired
	private ParametersActionsRepository parametersActionsRepository;

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

	public List<ParametersTriggers> readChannelParametersTriggers(Long channelId) {
		Channel channel = channelRepository.findOne(channelId);
		return parametersTriggersRepository.findByChannel(channel);
	}
	
	public List<ParametersTriggers> readChannelParametersTriggers(Long triggerId, Long channelId) {
		Trigger trigger = triggerRepository.findOne(triggerId);
		Channel channel = channelRepository.findOne(channelId);
		return parametersTriggersRepository.findByTriggerAndChannel(trigger, channel);
	}
	
	public List<ParametersActions> readChannelParametersActions(Long channelId) {
		Channel channel = channelRepository.findOne(channelId);
		return parametersActionsRepository.findByChannel(channel);
	}
	
	public List<ParametersActions> readChannelParametersActions(Long actionId, Long channelId) {
		Action action = actionRepository.findOne(actionId);
		Channel channel = channelRepository.findOne(channelId);
		return parametersActionsRepository.findByActionAndChannel(action, channel);
	}

	public ParametersTriggers readParameterTrigger(Long id) {
		return parametersTriggersRepository.findOne(id);
	}
	
	public ParametersActions readParameterAction(Long id) {
		return parametersActionsRepository.findOne(id);
	}
}