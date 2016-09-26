package if3t.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="timezones")
public class Timezone {

	@Id
    @Column(name = "id_timezone", nullable = false)
    private Long id;
	private String name;
	private boolean daylight_time;
	private int timezone_value;
	private String zone_id;

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

	public boolean isDaylight_time() {
		return daylight_time;
	}

	public void setDaylight_time(boolean daylight_time) {
		this.daylight_time = daylight_time;
	}

	public int getTimezone_value() {
		return timezone_value;
	}

	public void setTimezone_value(int timezone_value) {
		this.timezone_value = timezone_value;
	}
	
	public String getZone_id() {
		return zone_id;
	}

	public void setZone_id(String zone_id) {
		this.zone_id = zone_id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Timezone))
			return false;
		Timezone other = (Timezone) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}