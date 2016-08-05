package if3t.models;

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
@Table(name="parameters_triggers")
public class ParametersTriggers {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_param", nullable = false)
	private Long id;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name="id_channel")
	private Channel channel;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_trigger", nullable = false)
	private Trigger trigger;
	
	@NotNull
	@Column(name = "name", nullable = false)
	private String name;
	
	@NotNull
	@Column(name = "param_type", nullable = false)
	private String type;
	
	private String keyword;
	
	@OneToMany(mappedBy = "param")
	private Set<TriggerIngredient> trigger_ingredients;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
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
	
	public Set<TriggerIngredient> getTrigger_ingredients() {
		return trigger_ingredients;
	}

	public void setTrigger_ingredients(Set<TriggerIngredient> trigger_ingredients) {
		this.trigger_ingredients = trigger_ingredients;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((trigger == null) ? 0 : trigger.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ParametersTriggers))
			return false;
		ParametersTriggers other = (ParametersTriggers) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (trigger == null) {
			if (other.trigger != null)
				return false;
		} else if (!trigger.equals(other.trigger))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}