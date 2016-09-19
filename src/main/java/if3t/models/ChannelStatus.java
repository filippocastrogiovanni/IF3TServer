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
		result = prime * result + ((recipe == null) ? 0 : recipe.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ChannelStatus))
			return false;
		ChannelStatus other = (ChannelStatus) obj;
		if (recipe == null) {
			if (other.recipe != null)
				return false;
		} else if (!recipe.equals(other.recipe))
			return false;
		return true;
	}
}