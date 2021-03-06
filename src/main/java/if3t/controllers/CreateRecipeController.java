package if3t.controllers;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import if3t.entities.Action;
import if3t.entities.ParametersActions;
import if3t.entities.ParametersTriggers;
import if3t.entities.Trigger;
import if3t.services.CreateRecipeService;

@RestController
@CrossOrigin
public class CreateRecipeController 
{
	@Autowired
	private CreateRecipeService createRecipeService;
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/action/{id}", method=RequestMethod.GET)
	public Action getAction(@PathVariable Long id) {
		return createRecipeService.readAction(id);
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/actions/{channelId}", method=RequestMethod.GET)
	public List<Action> getChannelActions(@PathVariable Long channelId) {
		return createRecipeService.readChannelActions(channelId);
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/trigger/{id}", method=RequestMethod.GET)
	public Trigger getTrigger(@PathVariable Long id) {
		return createRecipeService.readTrigger(id);
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/triggers/{channelId}", method=RequestMethod.GET)
	public List<Trigger> getChannelTriggers(@PathVariable Long channelId) {
		return createRecipeService.readChannelTriggers(channelId);
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/parameter_trigger/{id}", method=RequestMethod.GET)
	public ParametersTriggers getParameterTrigger(@PathVariable Long id) {
		return createRecipeService.readParameterTrigger(id);
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/parameters_triggers/{channelId}", method=RequestMethod.GET)
	public List<ParametersTriggers> getChannelParametersTriggersByChannelId(@PathVariable Long channelId) {
		return createRecipeService.readChannelParametersTriggers(channelId);
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/parameter_action/{id}", method=RequestMethod.GET)
	public ParametersActions getParameterAction(@PathVariable Long id) {
		return createRecipeService.readParameterAction(id);
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/parameters_actions/{channelId}", method=RequestMethod.GET)
	public List<ParametersActions> getChannelParametersActions(@PathVariable Long channelId) {
		return createRecipeService.readChannelParametersActions(channelId);
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/channel_keywords/{triggerId}/{keyword}", method=RequestMethod.GET)
	public Set<String> getChannelKeywords(@PathVariable Long triggerId, @PathVariable String keyword) {
		return createRecipeService.readChannelKeywords(triggerId, keyword);
	}
}