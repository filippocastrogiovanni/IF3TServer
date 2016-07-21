package if3t.services;

import java.util.List;

import if3t.models.Action;
import if3t.models.ParametersActions;
import if3t.models.ParametersTriggers;
import if3t.models.Trigger;

public interface CreateRecipeService {

	public Action readAction(Long id);
	public List<Action> readChannelActions(Long channelId);
	public List<Trigger> readChannelTriggers(Long channelId);
	public Trigger readTrigger(Long id);
	public List<ParametersTriggers> readChannelParametersTriggers(Long channelId);
	public List<ParametersTriggers> readChannelParametersTriggers(Long triggerId, Long channelId);
	public List<ParametersActions> readChannelParametersActions(Long channelId);
	public List<ParametersActions> readChannelParametersActions(Long actionId, Long channelId);
	public ParametersTriggers readParameterTrigger(Long id);
	public ParametersActions readParameterAction(Long id);
}
