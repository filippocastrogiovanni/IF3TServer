package if3t.controllers;


import javax.naming.NoPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import if3t.exceptions.NotLoggedInException;
import if3t.gmail.AuthRequest;
import if3t.models.Response;
import if3t.models.User;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class GmailController {
	
	@Autowired
	private UserService userService;
	
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
		return new Response(req.toString(), 200);
	}
	
	@RequestMapping(value = "/gmail/authresponse?error={errorValue}", method = RequestMethod.GET)
	public Response gmailAuthResponseError(@PathVariable String errorValue) {
		return new Response(errorValue, 500);
	}
	
	@RequestMapping(value = "/gmail/authresponse?state={state}&code={code}", method = RequestMethod.GET)
	public Response gmailAuthResponseSuccess(@PathVariable String state, @PathVariable String code) {
		return new Response(state+ " " + code, 200);
	}


}
