package if3t.models;

import java.io.Serializable;

public class AuthorizationId implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Channel channel;
	private User user;
	
	public AuthorizationId() {
	}

	public AuthorizationId(Channel channel, User user) {
		super();
		this.channel = channel;
		this.user = user;
	}

	/**
	 * @return the channel
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AuthorizationId))
			return false;
		AuthorizationId other = (AuthorizationId) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
	
	
}
