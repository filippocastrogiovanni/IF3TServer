package if3t.models;

public class ParametersPOJO 
{
	private String name;
	private String type;
	
	public ParametersPOJO() {}
	
	public ParametersPOJO(ParametersTriggers pt) 
	{
		this.name = pt.getName();
		this.type = pt.getType();
	}
	
	public ParametersPOJO(ParametersActions pa) 
	{
		this.name = pa.getName();
		this.type = pa.getType();
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
}
