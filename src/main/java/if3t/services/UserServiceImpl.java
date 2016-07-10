package if3t.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import if3t.RequestPassword;
import if3t.exceptions.WrongPasswordException;
import if3t.models.Timezone;
import if3t.models.User;
import if3t.repositories.TimezoneRepository;
import if3t.repositories.UserRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private TimezoneRepository timezoneRepo;

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
		User targetUser = userRepo.findOne(user.getId());
		targetUser.setName(user.getName());
		targetUser.setSurname(user.getSurname());
		targetUser.setTimezone(user.getTimezone());
		userRepo.save(targetUser);
	}

	public User getUserByEmail(String email) {
		return userRepo.findByEmail(email);
	}
	
	public Timezone getTimezone(Long id) {
		return timezoneRepo.findOne(id);
	}

	public void changePassword(User user, RequestPassword passReq) throws WrongPasswordException {
		
		if(!user.getPassword().equals(new BCryptPasswordEncoder().encode(passReq.getCurrentPassword())))
			throw new WrongPasswordException("The current password doesn't match!");
		
		user.setPassword(new BCryptPasswordEncoder().encode(passReq.getNewPassword()));
		userRepo.save(user);
	}


}
