package if3t.apis;

import java.io.Serializable;
import java.util.Calendar;

import org.json.JSONObject;

public class GoogleRefreshTokenResponse implements Serializable {

	private static final long serialVersionUID = 63530490211386289L;
	private String access_token; 		// The token that can be sent to a Google API.
	private Long expires_in; 			// The remaining lifetime of the access token.
	private String token_type; 			// Identifies the type of token returned. At this
										// time, this field will always have the value
										// Bearer.
	private Long timestamp;
	private boolean valid = true;
	
	public GoogleRefreshTokenResponse(String jsonString) {
		JSONObject obj = new JSONObject(jsonString);
		try {
			this.access_token = obj.getString("access_token");
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

	public Long getExpires_in() {
		return expires_in;
	}

	public String getToken_type() {
		return token_type;
	}

	public boolean isValid() {
		return valid;
	}
	
	public Long getExpiration_date() {
		return this.timestamp + this.expires_in - 1;
	}

}
