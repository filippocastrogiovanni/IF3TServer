package if3t.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionPOJO 
{
	private Long id;
	private String channel_image_url;
	private String header;
	private String paragraph;
	private List<ParametersPOJO> parameters;
	
	public ActionPOJO() {}
	
	public ActionPOJO(Action act, List<ParametersActions> paList, Map<Long, ActionIngredient> aiMap)
	{
		this.id = act.getId();
		this.channel_image_url = act.getChannel().getImage_url();
		this.header = act.getHeader();
		this.paragraph = act.getParagraph();
		this.parameters = new ArrayList<ParametersPOJO>();

		for (ParametersActions pa : paList)
		{
			this.parameters.add(new ParametersPOJO(pa, aiMap.get(pa.getId())));
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getChannel_image_url() {
		return channel_image_url;
	}

	public void setChannel_image_url(String channel_image_url) {
		this.channel_image_url = channel_image_url;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getParagraph() {
		return paragraph;
	}

	public void setParagraph(String paragraph) {
		this.paragraph = paragraph;
	}

	public List<ParametersPOJO> getParameters() {
		return parameters;
	}

	public void setParameters(List<ParametersPOJO> parameters) {
		this.parameters = parameters;
	}
}