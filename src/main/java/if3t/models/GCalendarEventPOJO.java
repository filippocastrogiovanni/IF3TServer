package if3t.models;

public class GCalendarEventPOJO {
	private GCalendarDatePOJO start;
	private GCalendarDatePOJO end;
	private String description, title, location;
	
	public GCalendarDatePOJO getStart() {
		return start;
	}
	public void setStart(GCalendarDatePOJO start) {
		this.start = start;
	}
	public GCalendarDatePOJO getEnd() {
		return end;
	}
	public void setEnd(GCalendarDatePOJO end) {
		this.end = end;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	
}


