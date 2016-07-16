package if3t.apis;

import java.io.Serializable;

public class GoogleRefreshTokenRequest implements Serializable {
	
	private static final long serialVersionUID = 6914824311674585204L;

	private String token_uri;
	private String refresh_token;		//The refresh token returned from the authorization code exchange.
	private String client_id;			//The client ID obtained from the API Console.
	private String client_secret;		//The client secret obtained from the API Console.
	private String grant_type;			//As defined in the OAuth 2.0 specification, this field must contain a value of refresh_token.

	public GoogleRefreshTokenRequest(String ref_token) {
		super();
		this.refresh_token = ref_token;
		this.token_uri = "https://www.googleapis.com/oauth2/v4/token";
		this.client_id = "205247608184-qn9jd5afpqai7n8n6hbhb2qgvad7mih8.apps.googleusercontent.com";
		this.client_secret = "DPPiyrVcd-uqUMw7ponxFKv1";
		this.grant_type = "refresh_token";
	}

	public String getToken_uri() {
		return token_uri;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public String getClient_id() {
		return client_id;
	}

	public String getClient_secret() {
		return client_secret;
	}

	public String getGrant_type() {
		return grant_type;
	}
	
	public String getRequestBody() {
		StringBuilder builder = new StringBuilder();
		builder.append("client_id=" + this.client_id + "&");
		builder.append("client_secret=" + this.client_secret + "&");
		builder.append("refresh_token=" + this.refresh_token + "&");
		builder.append("grant_type=" + this.grant_type);

		return builder.toString();
	}

	
}
