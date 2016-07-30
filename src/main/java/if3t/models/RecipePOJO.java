package if3t.models;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

public class RecipePOJO 
{
	@NotNull(message = "error.recipe.id.null")
	private Long id;
	@NotNull(message = "error.recipe.description.null")
	private String description;
	private Boolean isPublic;
	private Boolean isEnabled;
	@NotNull(message = "error.recipe.username.null")
	@Length(min = 6, max = 30, message = "error.recipe.username.size")
	private String username;
	@Valid
	@NotNull(message = "error.recipe.trigger.null")
	private TriggerPOJO trigger;
	@Valid
	@NotNull(message = "error.recipe.actions.null")
	private List<ActionPOJO> actions;
	
	public RecipePOJO() {}
	
	public RecipePOJO(List<Recipe> recipes, TriggerPOJO trigger, List<ActionPOJO> actions)
	{
		this.id = recipes.get(0).getId();
		this.description = recipes.get(0).getDescription();
		this.isPublic = recipes.get(0).getIsPublic();
		this.isEnabled = recipes.get(0).getIsEnabled();
		this.username = recipes.get(0).getUser().getUsername();
		this.trigger = trigger;
		this.actions = actions;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public TriggerPOJO getTrigger() {
		return trigger;
	}

	public void setTrigger(TriggerPOJO trigger) {
		this.trigger = trigger;
	}

	public List<ActionPOJO> getActions() {
		return actions;
	}

	public void setActions(List<ActionPOJO> actions) {
		this.actions = actions;
	}

	@Override
	public String toString() {
		return "RecipePOJO [id=" + id + ", description=" + description + ", isPublic=" + isPublic + ", isEnabled="
				+ isEnabled + ", username=" + username + ", trigger=" + trigger + ", actions=" + actions + "]";
	}
}