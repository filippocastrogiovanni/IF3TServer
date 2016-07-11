package if3t.controllers;

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

import if3t.exceptions.InvalidParametersException;
import if3t.exceptions.NotLoggedInException;
import if3t.exceptions.WrongPasswordException;
import if3t.models.RequestPassword;
import if3t.models.Response;
import if3t.models.Role;
import if3t.models.User;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class UserController {

	@Autowired
	private UserService userService;

	String nameRegex = "^[a-zA-Z -\']{2,30}$";
	String surnameRegex = "^[a-zA-Z -\']{2,30}$";
	String emailRegex = "^[-a-z0-9~!$%^&*_=+}{\'?]+(.[-a-z0-9~!$%^&*_=+}{\'?]+)*@([a-z0-9_][-a-z0-9_]*(.[-a-z0-9_]+)*.(aero|arpa|biz|com|coop|edu|gov|info|int|mil|museum|name|net|org|pro|travel|mobi|[a-z][a-z])|([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}))(:[0-9]{1,5})?$";
	String usernameRegex = "^[a-zA-Z0-9]{6,30}$";
	String passwordRegex = "^[a-zA-Z0-9,!?.£%&;:-=]{6,30}$";

	@RequestMapping(value = "/signin", method = RequestMethod.POST)
	public Response createUser(@RequestBody User u) throws InvalidParametersException {
		User checkUser = null;

		if (!u.getName().matches(nameRegex))
			throw new InvalidParametersException("ERROR: name field is not valid");

		if (!u.getSurname().matches(surnameRegex))
			throw new InvalidParametersException("ERROR: surname field is not valid");
		
		if (!u.getEmail().matches(emailRegex))
			throw new InvalidParametersException("ERROR: e-mail field is not valid");
		
		checkUser = userService.getUserByEmail(u.getEmail());
		if (checkUser != null)
			throw new InvalidParametersException("ERROR: e-mail field is not valid");
		
		if (!u.getUsername().matches(usernameRegex))
			throw new InvalidParametersException("ERROR: username field is not valid");
		
		checkUser = userService.getUserByUsername(u.getUsername());
		if (checkUser != null)
			throw new InvalidParametersException("ERROR: username field is not valid");
		
		if (!u.getPassword().matches(passwordRegex))
			throw new InvalidParametersException("ERROR: password field is not valid");
		
		if (u.getTimezone() == null || u.getTimezone().getId() == null)
			throw new InvalidParametersException("ERROR: timezone field is not valid");
		
		if (!userService.getTimezone(u.getTimezone().getId()).equals(u.getTimezone()))
			throw new InvalidParametersException("ERROR: timezone field is not valid");
		
		u.setPassword(new BCryptPasswordEncoder().encode(u.getPassword()));
		u.setEnabled(true);
		u.setRole(Role.USER);
		userService.addUser(u);
		return new Response("Successful", 200);
	}

	@RequestMapping(value = "/userinfo", method = RequestMethod.PUT)
	public Response updateInfo(@RequestBody User u)
			throws NotLoggedInException, NoPermissionException, InvalidParametersException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		User loggedUser = userService.getUserByUsername(auth.getName());
		if (loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		if ((loggedUser.getId() != u.getId() && !loggedUser.getRole().equals(Role.ADMIN)) || !loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		

		boolean update = false;
		User curUser = loggedUser.getRole().equals(Role.ADMIN) ? userService.getUser(u.getId()) : loggedUser;

		if (curUser == null)
			throw new InvalidParametersException("ERROR: the user doesn't exist");

		if (curUser.getName() != u.getName()) {
			if (!u.getName().matches(nameRegex))
				throw new InvalidParametersException("ERROR: name field is not valid");
			update = true;
			curUser.setName(u.getName());
		}

		if (curUser.getSurname() != u.getSurname()) {
			if (!u.getSurname().matches(surnameRegex))
				throw new InvalidParametersException("ERROR: surname field is not valid");
			update = true;
			curUser.setSurname(u.getSurname());
		}

		if (curUser.getTimezone() != u.getTimezone()) {
			if (u.getTimezone() == null || u.getTimezone().getId() == null)
				throw new InvalidParametersException("ERROR: timezone field is not valid");

			if (!userService.getTimezone(u.getTimezone().getId()).equals(u.getTimezone()))
				throw new InvalidParametersException("ERROR: timezone field is not valid");
			update = true;
			curUser.setTimezone(u.getTimezone());
		}
		
		if (update) {
			userService.updateUser(curUser);
		}
		return new Response("Successful", 200);
	}

	@RequestMapping(value = "/userpassword", method = RequestMethod.PUT)
	public Response changePassword(@RequestBody RequestPassword passwordReq)
			throws NotLoggedInException, WrongPasswordException, NoPermissionException, InvalidParametersException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");
		
		User loggedUser = userService.getUserByUsername(auth.getName());
		if (loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");
		
		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		
		if (!passwordReq.getCurrentPassword().matches(passwordRegex))
			throw new InvalidParametersException("ERROR: current password field is not valid");
		
		if (!new BCryptPasswordEncoder().matches(passwordReq.getCurrentPassword(), loggedUser.getPassword()))
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		
		if (!passwordReq.getNewPassword().matches(passwordRegex))
			throw new InvalidParametersException("ERROR: new password field is not valid");
		
		if(passwordReq.getNewPassword().equals(passwordReq.getCurrentPassword()))
			throw new InvalidParametersException("ERROR: you are not changing the password");
		
		loggedUser.setPassword(new BCryptPasswordEncoder().encode(passwordReq.getNewPassword()));
		userService.updateUser(loggedUser);
		return new Response("Successful", 200);
	}

}
