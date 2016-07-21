package if3t.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import if3t.models.Action;
import if3t.models.ParametersActions;
import if3t.models.ParametersTriggers;
import if3t.models.Trigger;
import if3t.services.CreateRecipeService;

@RestController
@CrossOrigin
public class CreateRecipeController {

	@Autowired
	private CreateRecipeService createRecipeService;
	
	
	@RequestMapping(value="/action/{id}", method=RequestMethod.GET)
	public Action getAction(@PathVariable Long id) {
		return createRecipeService.readAction(id);
	}
	
	@RequestMapping(value="/actions/{channelId}", method=RequestMethod.GET)
	public List<Action> getChannelActions(@PathVariable Long channelId) {
		return createRecipeService.readChannelActions(channelId);
	}
	
	@RequestMapping(value="/trigger/{id}", method=RequestMethod.GET)
	public Trigger getTrigger(@PathVariable Long id) {
		return createRecipeService.readTrigger(id);
	}
	
	@RequestMapping(value="/triggers/{channelId}", method=RequestMethod.GET)
	public List<Trigger> getChannelTriggers(@PathVariable Long channelId) {
		return createRecipeService.readChannelTriggers(channelId);
	}
	
	@RequestMapping(value="/parameter_trigger/{id}", method=RequestMethod.GET)
	public ParametersTriggers getParameterTrigger(@PathVariable Long id) {
		return createRecipeService.readParameterTrigger(id);
	}
	
	@RequestMapping(value="/parameters_triggers/{channelId}", method=RequestMethod.GET)
	public List<ParametersTriggers> getChannelParametersTriggersByChannelId(@PathVariable Long channelId) {
		return createRecipeService.readChannelParametersTriggers(channelId);
	}
	
	@RequestMapping(value="/parameter_action/{id}", method=RequestMethod.GET)
	public ParametersActions getParameterAction(@PathVariable Long id) {
		return createRecipeService.readParameterAction(id);
	}
	
	@RequestMapping(value="/parameters_actions/{channelId}", method=RequestMethod.GET)
	public List<ParametersActions> getChannelParametersActions(@PathVariable Long channelId) {
		return createRecipeService.readChannelParametersActions(channelId);
	}
}
