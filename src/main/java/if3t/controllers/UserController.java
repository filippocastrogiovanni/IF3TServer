package if3t.controllers;

import javax.naming.NoPermissionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import if3t.RequestPassword;
import if3t.exceptions.NotLoggedInException;
import if3t.exceptions.WrongPasswordException;
import if3t.models.Role;
import if3t.models.User;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/signin", method = RequestMethod.POST)
	public void createUser(@RequestBody User u) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			User user = userService.getUserByUsername(auth.getName());
			if (user != null) {
				//TODO return error
			}
		}
		//TODO fare controlli su user
		
		u.setPassword(new BCryptPasswordEncoder().encode(u.getPassword()));
		u.setEnabled(true);
		u.setRole(Role.USER);
		userService.addUser(u);
	}
	
	@RequestMapping(value="/users/{username}", method=RequestMethod.GET)
	public User getUser(@PathVariable String username) {
		return userService.getUserByUsername(username);
	}
	
	@RequestMapping(value="/userpassword", method=RequestMethod.PUT)
	public void changePassword(@RequestBody RequestPassword passwordReq) throws NotLoggedInException, WrongPasswordException{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");
			
		User loggedUser = userService.getUserByUsername(auth.getName());
		if(loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");
		
		userService.changePassword(loggedUser, passwordReq);
	}
	
	@RequestMapping(value="/userinfo", method=RequestMethod.PUT)
	public void updateInfo(@RequestBody User u) throws NotLoggedInException, NoPermissionException{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");
			
		User loggedUser = userService.getUserByUsername(auth.getName());
		if(loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");
		
		if(loggedUser.getId() != u.getId() && !u.getRole().equals(Role.ADMIN))
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		
		userService.updateUser(u);
	}

}
