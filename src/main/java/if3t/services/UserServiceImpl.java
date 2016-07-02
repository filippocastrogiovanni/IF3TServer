package if3t.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import if3t.models.User;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	//private UserRepository userRepo;

	public User getUserByUsername(String username) {
		return null;//userRepo.findByUsername(username);
	}

}
