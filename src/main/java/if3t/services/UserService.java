package if3t.services;

import if3t.models.Timezone;
import if3t.models.User;

public interface UserService {

	public User getUser(Long id);
	public User getUserByUsername(String username);
	public void addUser(User user);
	public void deleteUser(Long id);
	public void updateUser(User user);
	public User getUserByEmail(String email);
	public Timezone getTimezone(Long id);
}
