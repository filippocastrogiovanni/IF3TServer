package if3t.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	@Override
	public List<Action> readChannelActions(Long channelId) {
		Channel channel = channelRepository.findOne(channelId);
		return actionRepository.findByChannel(channel);
	}

	@Override
	public Action readAction(Long id) {
		return actionRepository.findOne(id);
	}

	@Override
	public List<Trigger> readChannelTriggers(Long channelId) {
		Channel channel = channelRepository.findOne(channelId);
		return triggerRepository.findByChannel(channel);
	}

	@Override
	public Trigger readTrigger(Long id) {
		return triggerRepository.findOne(id);
	}

	@Override
	public List<ParametersTriggers> readChannelParametersTriggers(Long channelId) 
	{
		Channel channel = channelRepository.findOne(channelId);
		return parametersTriggersRepository.findByChannel(channel);
	}
	
	@Override
	public List<ParametersTriggers> readChannelParametersTriggers(Long triggerId, Long channelId) 
	{
		Trigger trigger = triggerRepository.findOne(triggerId);
		Channel channel = channelRepository.findOne(channelId);
		return parametersTriggersRepository.findByTriggerAndChannel(trigger, channel);
	}
	
	@Override
	public List<ParametersActions> readChannelParametersActions(Long channelId) 
	{
		Channel channel = channelRepository.findOne(channelId);
		return parametersActionsRepository.findByChannel(channel);
	}
	
	@Override
	public List<ParametersActions> readChannelParametersActions(Long actionId, Long channelId) 
	{
		Action action = actionRepository.findOne(actionId);
		Channel channel = channelRepository.findOne(channelId);
		return parametersActionsRepository.findByActionAndChannel(action, channel);
	}

	@Override
	public ParametersTriggers readParameterTrigger(Long id) {
		return parametersTriggersRepository.findOne(id);
	}
	
	@Override
	public ParametersActions readParameterAction(Long id) {
		return parametersActionsRepository.findOne(id);
	}

	@Override
	public Set<String> readChannelKeywords(Long triggerId, String keyword) 
	{
		Set<String> keywords = new HashSet<String>();
		
		for (ParametersTriggers pt : parametersTriggersRepository.findByTrigger_idAndIsSendable(triggerId, true))
		{
			keywords.add(pt.getKeyword());
		}
		
		for (ParametersActions at : parametersActionsRepository.findByChannel_keyword(keyword))
		{
			keywords.add(at.getKeyword());
		}
		
		return keywords;
	}
}