package if3t.services;

import java.util.List;

import if3t.models.Action;
import if3t.models.Channel;
import if3t.models.Parameter;
import if3t.models.Trigger;

public interface CreateRecipeService {

	public List<Channel> readChannels();
	public Channel readChannel(Long id);
	public List<Action> readChannelActions(Long channelId);
	public Action readAction(Long id);
	public List<Trigger> readChannelTriggers(Long channelId);
	public Trigger	readTrigger(Long id);
	public List<Parameter> readChannelParameters(Long channelId);
	public Parameter readParameter(Long id);
}
