package if3t.entities;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name="recipes")
public class Recipe {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_recipe", nullable = false)
	private Long id;
	
	@Column(name = "id_group", nullable = false)
	private String groupId;
	
	private String description;
	
	@Column(name = "is_public", nullable = false)
	private Boolean isPublic;
	
	@Column(name = "is_enabled", nullable = false)
	private Boolean isEnabled;
	
	@ManyToOne()
	@JoinColumn(name = "id_user")
	@JsonBackReference(value="recipe-user")
	private User user;
	
	@OneToOne()
	@JoinColumn(name = "id_trigger")
	private Trigger trigger;
	
	@OneToOne()
	@JoinColumn(name = "id_action")
	private Action action;
	
	@OneToMany(mappedBy = "recipe")
	private Set<TriggerIngredient> trigger_ingredients;
	
	@OneToMany(mappedBy = "recipe")
	private Set<ActionIngredient> action_ingredients;
	
	@OneToMany(mappedBy = "recipe")
	@JsonBackReference(value="recipe-channels_statuses")
	private Set<ChannelStatus> channels_statuses;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public Boolean getIsEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Set<TriggerIngredient> getTrigger_ingredients() {
		return trigger_ingredients;
	}

	public void setTrigger_ingredients(Set<TriggerIngredient> trigger_ingredients) {
		this.trigger_ingredients = trigger_ingredients;
	}

	public Set<ActionIngredient> getAction_ingredients() {
		return action_ingredients;
	}

	public void setAction_ingredients(Set<ActionIngredient> action_ingredients) {
		this.action_ingredients = action_ingredients;
	}

	public Set<ChannelStatus> getChannels_statuses() {
		return channels_statuses;
	}

	public void setChannels_statuses(Set<ChannelStatus> channels_statuses) {
		this.channels_statuses = channels_statuses;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((isEnabled == null) ? 0 : isEnabled.hashCode());
		result = prime * result + ((isPublic == null) ? 0 : isPublic.hashCode());
		result = prime * result + ((trigger == null) ? 0 : trigger.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Recipe))
			return false;
		Recipe other = (Recipe) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (isEnabled == null) {
			if (other.isEnabled != null)
				return false;
		} else if (!isEnabled.equals(other.isEnabled))
			return false;
		if (isPublic == null) {
			if (other.isPublic != null)
				return false;
		} else if (!isPublic.equals(other.isPublic))
			return false;
		if (trigger == null) {
			if (other.trigger != null)
				return false;
		} else if (!trigger.equals(other.trigger))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	
}