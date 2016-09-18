package if3t.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity()
@Table(name="channels_statuses")
public class ChannelStatus 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status", nullable = false)
	private Long id;
	
	@ManyToOne()
	@JoinColumn(name = "id_recipe")
	private Recipe recipe;
	
	@Column(name = "since_ref")
	private Long sinceRef;
	
	@Column(name = "page_token")
	private String pageToken;

	@Column(name = "facebook_since_ref")
	private Long facebookSinceRef;

	@Column(name = "facebook_full_name")
	private String facebookFullName;
	
	@Column(name = "facebook_profile_picture")
	private String facebookProfilePicture;
	
	@Column(name = "facebook_location")
	private String facebookLocation;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

	public Long getSinceRef() {
		return sinceRef;
	}

	public void setSinceRef(Long sinceRef) {
		this.sinceRef = sinceRef;
	}

	public String getPageToken() {
		return pageToken;
	}

	public void setPageToken(String pageToken) {
		this.pageToken = pageToken;
	}

	public Long getFacebookSinceRef() {
		return facebookSinceRef;
	}

	public void setFacebookSinceRef(Long facebookSinceRef) {
		this.facebookSinceRef = facebookSinceRef;
	}

	public String getFacebookFullName() {
		return facebookFullName;
	}

	public void setFacebookFullName(String facebookFullName) {
		this.facebookFullName = facebookFullName;
	}

	public String getFacebookProfilePicture() {
		return facebookProfilePicture;
	}

	public void setFacebookProfilePicture(String facebookProfilePicture) {
		this.facebookProfilePicture = facebookProfilePicture;
	}

	public String getFacebookLocation() {
		return facebookLocation;
	}

	public void setFacebookLocation(String facebookLocation) {
		this.facebookLocation = facebookLocation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facebookFullName == null) ? 0 : facebookFullName.hashCode());
		result = prime * result + ((facebookLocation == null) ? 0 : facebookLocation.hashCode());
		result = prime * result + ((facebookProfilePicture == null) ? 0 : facebookProfilePicture.hashCode());
		result = prime * result + ((facebookSinceRef == null) ? 0 : facebookSinceRef.hashCode());
		result = prime * result + ((pageToken == null) ? 0 : pageToken.hashCode());
		result = prime * result + ((sinceRef == null) ? 0 : sinceRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChannelStatus other = (ChannelStatus) obj;
		if (facebookFullName == null) {
			if (other.facebookFullName != null)
				return false;
		} else if (!facebookFullName.equals(other.facebookFullName))
			return false;
		if (facebookLocation == null) {
			if (other.facebookLocation != null)
				return false;
		} else if (!facebookLocation.equals(other.facebookLocation))
			return false;
		if (facebookProfilePicture == null) {
			if (other.facebookProfilePicture != null)
				return false;
		} else if (!facebookProfilePicture.equals(other.facebookProfilePicture))
			return false;
		if (facebookSinceRef == null) {
			if (other.facebookSinceRef != null)
				return false;
		} else if (!facebookSinceRef.equals(other.facebookSinceRef))
			return false;
		if (pageToken == null) {
			if (other.pageToken != null)
				return false;
		} else if (!pageToken.equals(other.pageToken))
			return false;
		if (sinceRef == null) {
			if (other.sinceRef != null)
				return false;
		} else if (!sinceRef.equals(other.sinceRef))
			return false;
		return true;
	}
	
	
}