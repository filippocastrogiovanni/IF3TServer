package if3t.models;

public class GCalendarEventPOJO {
	private GCalendarDatePojo start;
	private GCalendarDatePojo end;
	private String description, title, location;
	
	public GCalendarDatePojo getStart() {
		return start;
	}
	public void setStart(GCalendarDatePojo start) {
		this.start = start;
	}
	public GCalendarDatePojo getEnd() {
		return end;
	}
	public void setEnd(GCalendarDatePojo end) {
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


