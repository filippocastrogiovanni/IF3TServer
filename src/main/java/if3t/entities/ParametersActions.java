package if3t.entities;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.persistence.Id;

@Entity
@Table(name="parameters_actions")
public class ParametersActions {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_param", nullable = false)
	private Long id;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name="id_channel")
	private Channel channel;
	
	@NotNull
	@Column(name = "name", nullable = false)
	private String name;
	
	@NotNull
	@Column(name = "param_type", nullable = false)
	private String type;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_action", nullable = false)
	private Action action;
	
	private String keyword;
	
	@OneToMany(mappedBy = "param")
	private Set<ActionIngredient> action_ingredients;
	
	@Column(name = "can_receive")
	private Boolean canReceive;
	
	@Column(name = "max_length")
	private int maxLength;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}
	
	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * @param keyword the keyword to set
	 */
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	public Set<ActionIngredient> getAction_ingredients() {
		return action_ingredients;
	}

	public void setAction_ingredients(Set<ActionIngredient> action_ingredients) {
		this.action_ingredients = action_ingredients;
	}

	public Boolean getCanReceive() {
		return canReceive;
	}

	public void setCanReceive(Boolean canReceive) {
		this.canReceive = canReceive;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
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
		if (!(obj instanceof ParametersActions))
			return false;
		ParametersActions other = (ParametersActions) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (keyword == null) {
			if (other.keyword != null)
				return false;
		} else if (!keyword.equals(other.keyword))
			return false;
		return true;
	}
}
