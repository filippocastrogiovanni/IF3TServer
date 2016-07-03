package if3t.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import if3t.models.Action;
import if3t.models.Channel;
import if3t.models.Parameter;
import if3t.models.Trigger;
import if3t.services.CreateRecipeService;

@RestController
@CrossOrigin
public class CreateRecipeController {

	@Autowired
	private CreateRecipeService createRecipeService;
	
	@RequestMapping(value="/channel/{id}", method=RequestMethod.GET)
	public Channel getChannel(@PathVariable Long id) {
		return createRecipeService.readChannel(id);
	}
	
	@RequestMapping(value="/channels", method=RequestMethod.GET)
	public List<Channel> getChannels() {
		return createRecipeService.readChannels();
	}
	
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
	
	@RequestMapping(value="/param/{id}", method=RequestMethod.GET)
	public Parameter getParameter(@PathVariable Long id) {
		return createRecipeService.readParameter(id);
	}
	
	@RequestMapping(value="/params/{channelId}", method=RequestMethod.GET)
	public List<Parameter> getChannelParameters(@PathVariable Long channelId) {
		return createRecipeService.readChannelParameters(channelId);
	}
}
