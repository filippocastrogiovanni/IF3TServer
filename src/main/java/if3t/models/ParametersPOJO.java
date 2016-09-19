package if3t.models;

import if3t.entities.ActionIngredient;
import if3t.entities.ParametersActions;
import if3t.entities.ParametersTriggers;
import if3t.entities.TriggerIngredient;

public class ParametersPOJO 
{
	private Long id;
	private String name;
	private String type;
	private String value;
	
	public ParametersPOJO() {}
	
	public ParametersPOJO(ParametersTriggers pt, TriggerIngredient ti) 
	{
		this.id = pt.getId();
		this.name = pt.getName();
		this.type = pt.getType();
		this.value = (ti != null) ? ti.getValue() : "";
	}
	
	public ParametersPOJO(ParametersActions pa, ActionIngredient ai) 
	{
		this.id = pa.getId();
		this.name = pa.getName();
		this.type = pa.getType();
		this.value = (ai != null) ? ai.getValue() : "";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ParametersPOJO [id=" + id + ", name=" + name + ", type=" + type + ", value=" + value + "]";
	}
}