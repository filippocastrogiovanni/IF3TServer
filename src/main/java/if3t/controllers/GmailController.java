package if3t.controllers;

import java.io.IOException;
import java.util.List;

import javax.naming.NoPermissionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;

import if3t.RequestPassword;
import if3t.Response;
import if3t.exceptions.InvalidParametersException;
import if3t.exceptions.NotLoggedInException;
import if3t.exceptions.WrongPasswordException;
import if3t.models.User;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class GmailController {
	
	@Autowired
	private UserService userService;
	
	@RequestMapping(value = "/gmailauth", method = RequestMethod.GET)
	public Response gmailAuth()
			throws NotLoggedInException, NoPermissionException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");
		
		User loggedUser = userService.getUserByUsername(auth.getName());
		if (loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");
		
		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		
		return new Response("Successful", 200);
	}
	
	@RequestMapping(value = "/gmailresponse", method = RequestMethod.GET)
	public Response gmailAuthResponse() {
		return null;
	}


}
