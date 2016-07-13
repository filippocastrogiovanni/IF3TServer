package if3t.apis;

import java.io.Serializable;

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
	private String expires_in; // The remaining lifetime of the access token.
	private String token_type; // Identifies the type of token returned. At this
								// time, this field will always have the value
								// Bearer.

	public GoogleTokenResponse() {
	}

	public GoogleTokenResponse(String access_token, String refresh_token, String expires_in, String token_type) {
		super();
		this.access_token = access_token;
		this.refresh_token = refresh_token;
		this.expires_in = expires_in;
		this.token_type = token_type;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}

	public String getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(String expires_in) {
		this.expires_in = expires_in;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

}
