package if3t.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import if3t.Response;
import if3t.exceptions.NotLoggedInException;
import if3t.models.Channel;
import if3t.models.User;
import if3t.services.ChannelService;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class ChannelController {

	@Autowired
	private ChannelService channelService;
	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/channel/{id}", method=RequestMethod.GET)
	public Channel getChannel(@PathVariable Long id) {
		return channelService.readChannel(id);
	}
	
	@RequestMapping(value="/channels", method=RequestMethod.GET)
	public List<Channel> getChannels() {
		return channelService.readChannels();
	}
	
	@RequestMapping(value="/authorized_channels", method=RequestMethod.GET)
	public List<Channel> getUserChannels() throws NotLoggedInException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth == null)
			throw new NotLoggedInException("ERROR: not loggedIn");
		
		user = userService.getUserByUsername(auth.getName());
		return channelService.readUserChannels(user.getId());
	}
	
	@RequestMapping(value="/unauthorize_channel/{channelId}", method=RequestMethod.POST)
	public Response unautorizeChannel(@PathVariable Long channelId) throws NotLoggedInException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth == null)
			throw new NotLoggedInException("ERROR: not loggedIn");
		
		user = userService.getUserByUsername(auth.getName());
		channelService.unautorizeChannel(user.getId(), channelId);
		return new Response("Successful", 200);
	}
}
