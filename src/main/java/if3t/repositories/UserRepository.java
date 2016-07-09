package if3t.repositories;

import org.springframework.data.repository.Repository;
import if3t.models.User;

public interface UserRepository extends Repository<User, Long> {

	public Iterable<User> findAll();
	public User findOne(Long id);
	public User findByUsername(String username);
	public User save(User user);
	public void delete(User user);
	public User findByEmail(String email);
	public User findByIdAndChannels_ChannelId(Long id, Long channelId);
}
