package if3t.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import if3t.models.CurrentUser;
import if3t.models.User;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserService userService;
	
	@Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }
	
	public CurrentUser loadUserByUsername(String username) throws UsernameNotFoundException {
	    User user = userService.getUserByUsername(username);
	    if(user == null)
	    	new UsernameNotFoundException(String.format("User with username <%s> was not found", username));
	    return new CurrentUser(user);
	}

}
