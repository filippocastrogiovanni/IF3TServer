package if3t.models;

import java.util.List;

public class RecipePOJO 
{
	private Long id;
	private String description;
	private Boolean isPublic;
	private Boolean isEnabled;
	private TriggerPOJO trigger;
	private List<ActionPOJO> actions;
	
	public RecipePOJO() {}
	
	public RecipePOJO(List<Recipe> recipes, TriggerPOJO trigger, List<ActionPOJO> actions)
	{
		this.id = recipes.get(0).getId();
		this.description = recipes.get(0).getDescription();
		this.isPublic = recipes.get(0).getIsPublic();
		this.isEnabled = recipes.get(0).getIsEnabled();
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

	public TriggerPOJO getTrigger() {
		return trigger;
	}

	public void setTriggerInfo(TriggerPOJO trigger) {
		this.trigger = trigger;
	}

	public List<ActionPOJO> getActions() {
		return actions;
	}

	public void setActions(List<ActionPOJO> actions) {
		this.actions = actions;
	}
}