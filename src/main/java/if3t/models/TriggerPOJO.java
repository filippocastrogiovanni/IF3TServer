package if3t.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import if3t.annotations.Parameters;
import if3t.annotations.Parameters.ValidationMode;

public class TriggerPOJO 
{
	@NotNull(message = "error.trigger.id.null")
	private Long id;
	private String channel_image_url;
	private String header;
	private String paragraph;
	@Parameters(mode = ValidationMode.TRIGGER)
	@NotNull(message = "error.trigger.parameters.null")
	private List<ParametersPOJO> parameters;
	
	public TriggerPOJO() {}
	
	public TriggerPOJO(Trigger trig, List<ParametersTriggers> ptList, Map<Long, TriggerIngredient> tiMap)
	{
		this.id = trig.getId();
		this.channel_image_url = trig.getChannel().getImage_url();
		this.header = trig.getHeader();
		this.paragraph = trig.getParagraph();
		this.parameters = new ArrayList<ParametersPOJO>();

		for (ParametersTriggers pt : ptList)
		{
			this.parameters.add(new ParametersPOJO(pt, tiMap.get(pt.getId())));
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

	@Override
	public String toString() {
		return "TriggerPOJO [id=" + id + ", channel_image_url=" + channel_image_url + ", header=" + header
				+ ", paragraph=" + paragraph + ", parameters=" + parameters + "]";
	}
}