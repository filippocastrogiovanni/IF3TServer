package if3t.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="timezones")
public class Timezone {

	@Id
    @Column(name = "id_timezone", nullable = false)
    private Long id;
	
	@NotNull
	@Column(name = "name", nullable = false)
	private String name;

	@NotNull
	@Column(name = "daylight_time", nullable = false)
	private boolean daylight_time;
	
	@NotNull
	@Column(name = "timezone_value", nullable = false)
	private int timezone_value;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (daylight_time ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + timezone_value;
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
		if (daylight_time != other.daylight_time)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (timezone_value != other.timezone_value)
			return false;
		return true;
	}
}