package if3t.repositories;

import org.springframework.data.repository.Repository;
import if3t.models.User;

public interface UserRepository extends Repository<User, Long> {

	public Iterable<User> findAll();
	public User findByUsername(String username);
	public User save(User booking);
	public void delete(User booking);
}
