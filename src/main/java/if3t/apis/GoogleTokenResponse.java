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
		
		if(obj.getString("access_token") != null) {
			this.access_token = obj.getString("access_token");
		} else {
			valid = false;
		}
		
		if(obj.getString("refresh_token") != null) {
			this.refresh_token = obj.getString("refresh_token");
		} else {
			valid = false;
		}
		
		if(obj.getString("expires_in") != null) {
			this.expires_in = Long.parseLong(obj.getString("expires_in"));
		} else {
			valid = false;
		}
		
		if(obj.getString("token_type") != null) {
			this.token_type = obj.getString("token_type");
		} else {
			valid = false;
		}
		
		Calendar now = Calendar.getInstance();
		this.timestamp = now.getTimeInMillis()/1000;
		
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
		return this.timestamp+this.expires_in-1;
		
	}

	public String getToken_type() {
		return token_type;
	}

	public boolean isValid() {
		return valid;
	}

}
