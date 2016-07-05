package if3t.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import if3t.models.User;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public void createUser(@RequestBody User u) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			User user = userService.getUserByUsername(auth.getName());
			if (user != null) {
				//TODO return error
			}
		}
		//TODO fare controlli su user
		//TODO salvare la password come hash
		u.setPassword(new BCryptPasswordEncoder().encode(u.getPassword()));
		userService.addUser(u);
	}

}
