package if3t.controllers;

import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NoPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import if3t.exceptions.NotLoggedInException;
import if3t.gmail.AuthRequest;
import if3t.models.Response;
import if3t.models.User;
import if3t.services.ChannelService;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class GmailController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private ChannelService channelService;
	
	private ConcurrentHashMap<String, String> authRequests = new ConcurrentHashMap<String, String>();

	@RequestMapping(value = "/gmail/auth", method = RequestMethod.GET)
	public Response gmailAuth() throws NotLoggedInException, NoPermissionException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		User loggedUser = userService.getUserByUsername(auth.getName());
		if (loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		AuthRequest req = new AuthRequest(loggedUser);
		authRequests.put(req.getState(), loggedUser.getUsername());
		return new Response(req.toString(), 200);
	}

	@RequestMapping(value = "/gmail/authresponse", method = RequestMethod.GET)
	public String gmailAuthResponse(@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error)
			throws NotLoggedInException, NoPermissionException {

		if (error != null) {
			// TODO creare GMAil exception
			return "<h1>ERROR: connection refused</h1>";
		}
		
		if (state == null)
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		
		if(!authRequests.containsKey(state))
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		
		String username = authRequests.get(state);
		authRequests.remove(state);
		User loggedUser = userService.getUserByUsername(username);
		if (loggedUser == null)
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		
		if(code == null)
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		channelService.authorizeChannel(loggedUser.getId(), (long) 1, code);
		
		return "<script>window.close();</script>";
	}

}
