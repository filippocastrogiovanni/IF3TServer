package if3t.models;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.Id;

@Entity
@Table(name="channels")
public class Channel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_channel", nullable = false)
	private Long channelId;
	
	@NotNull
	private String name;
	
	@NotNull
	private String image_url;
	
	@NotNull
	private String keyword;
	
	@Column(name = "is_needed_auth", nullable = false)
	private Boolean isNeededAuth;

	@OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
	@JsonBackReference(value="channel-authorizations")
	private Set<Authorization> authorizations;
	 
	public Long getChannelId() {
		return channelId;
	}

	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage_url() {
		return image_url;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Boolean getIsNeededAuth() {
		return isNeededAuth;
	}

	public void setIsNeededAuth(Boolean isNeededAuth) {
		this.isNeededAuth = isNeededAuth;
	}
	
	public Set<Authorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Set<Authorization> autorizations) {
		this.authorizations = autorizations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Channel))
			return false;
		Channel other = (Channel) obj;
		if (keyword == null) {
			if (other.keyword != null)
				return false;
		} else if (!keyword.equals(other.keyword))
			return false;
		return true;
	}
}