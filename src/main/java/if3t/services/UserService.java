package if3t.services;

import if3t.RequestPassword;
import if3t.exceptions.WrongPasswordException;
import if3t.models.Timezone;
import if3t.models.User;

public interface UserService {

	public User getUserByUsername(String username);
	public void addUser(User user);
	public void deleteUser(Long id);
	public void updateUser(User user);
	public User getUserByEmail(String email);
	public Timezone getTimezone(Long id);
	public void changePassword(User user, RequestPassword passReq) throws WrongPasswordException;
}
