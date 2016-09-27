package if3t.controllers;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import if3t.entities.Channel;
import if3t.entities.User;
import if3t.exceptions.NotLoggedInException;
import if3t.services.ChannelService;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class ChannelController {

	@Autowired
	private ChannelService channelService;
	@Autowired
	private UserService userService;
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/channel/{id}", method=RequestMethod.GET)
	public Channel getChannel(@PathVariable Long id) {
		return channelService.readChannel(id);
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/trigger_channels", method=RequestMethod.GET)
	public Set<Channel> getTriggerChannels() {
		return channelService.getChannelsForTrigger();
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/action_channels", method=RequestMethod.GET)
	public Set<Channel> getActionChannels() {
		return channelService.getChannelsForAction();
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/channels", method=RequestMethod.GET)
	public List<Channel> getChannels() {
		return channelService.readChannels();
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/authorized_channels", method=RequestMethod.GET)
	public List<Channel> getUserChannels() throws NotLoggedInException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth == null)
			throw new NotLoggedInException("ERROR: not loggedIn");
		
		user = userService.getUserByUsername(auth.getName());
		return channelService.readUserChannels(user.getId());
	}
	
	/*
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/unauthorize_channel/{channelId}", method=RequestMethod.POST)
	public Response unauthorizeChannel(@PathVariable Long channelId) throws NotLoggedInException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth == null)
			throw new NotLoggedInException("ERROR: not loggedIn");
		
		user = userService.getUserByUsername(auth.getName());
		channelService.unautorizeChannel(user.getId(), channelId);
		return new Response("Successful", HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase());
	}
	*/
}