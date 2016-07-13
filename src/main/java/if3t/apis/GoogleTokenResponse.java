package if3t.apis;

import java.io.Serializable;
import java.util.Calendar;

import org.json.JSONObject;

public class GoogleTokenResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 827192631964662672L;
	private String access_token; // The token that can be sent to a Google API.
	private String refresh_token; // A token that may be used to obtain a new
									// access token. Refresh tokens are valid
									// until the user revokes access. This field
									// is only present if access_type=offline is
									// included in the authorization code
									// request.
	private Long expires_in; // The remaining lifetime of the access token.
	private String token_type; // Identifies the type of token returned. At this
								// time, this field will always have the value
								// Bearer.
	private Long timestamp;
	private boolean valid = true;

	public GoogleTokenResponse(String jsonString) {
		JSONObject obj = new JSONObject(jsonString);
		try {
			this.access_token = obj.getString("access_token");
			this.refresh_token = obj.getString("refresh_token");
			this.expires_in = new Long(obj.getInt("expires_in"));
			this.token_type = obj.getString("token_type");
			Calendar now = Calendar.getInstance();
			this.timestamp = now.getTimeInMillis() / 1000;
		} catch (Exception e) {
			valid = false;
		}

	}

	public String getAccess_token() {
		return access_token;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public Long getExpires_in() {
		return expires_in;
	}

	public Long getExpiration_date() {
		return this.timestamp + this.expires_in - 1;

	}

	public String getToken_type() {
		return token_type;
	}

	public boolean isValid() {
		return valid;
	}

}
