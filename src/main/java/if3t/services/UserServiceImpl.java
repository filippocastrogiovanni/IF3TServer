package if3t.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import if3t.models.User;
import if3t.repositories.UserRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;

	public User getUserByUsername(String username) {
		return userRepo.findByUsername(username);
	}

	public void addUser(User user) {
		userRepo.save(user);
	}

	public void deleteUser(Long id) {
		User user = userRepo.findOne(id);
		userRepo.delete(user);
	}

	public void updateUser(User user) {
		userRepo.save(user);
	}

	public User getUserByEmail(String email) {
		return userRepo.findByEmail(email);
	}

}
