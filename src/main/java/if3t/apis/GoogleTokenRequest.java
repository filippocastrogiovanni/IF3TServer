package if3t.apis;

import java.io.Serializable;

public class GoogleTokenRequest implements Serializable {

	private static final long serialVersionUID = -4785786466577755789L;
	private String token_uri;
	private String code; 			// The authorization code returned from the initial request.
	private String client_id; 		// The client ID obtained from the API Console.
	private String client_secret; 	// The client secret obtained from the API Console.
	private String redirect_uri; 	// One of the redirect URIs listed for this project in the API Console.
	private String grant_type; 		// As defined in the OAuth 2.0 specification, this field must contain a value of authorization_code.

	public GoogleTokenRequest(String code) {
		super();
		this.code = code;
		this.token_uri = "https://accounts.google.com/o/oauth2/token";
		this.client_id = "205247608184-qn9jd5afpqai7n8n6hbhb2qgvad7mih8.apps.googleusercontent.com";
		this.client_secret = "DPPiyrVcd-uqUMw7ponxFKv1";
		this.grant_type = "authorization_code";
		this.redirect_uri = "http://localhost:8181/gmail/authresponse";
	}

	public String getToken_uri() {
		return token_uri;
	}

	public String getCode() {
		return code;
	}

	public String getClient_id() {
		return client_id;
	}

	public String getClient_secret() {
		return client_secret;
	}

	public String getRedirect_uri() {
		return redirect_uri;
	}

	public String getGrant_type() {
		return grant_type;
	}

	public String getRequestBody() {
		StringBuilder builder = new StringBuilder();
		builder.append("code=" + this.code + "&");
		builder.append("client_id=" + this.client_id + "&");
		builder.append("client_secret=" + this.client_secret + "&");
		builder.append("redirect_uri=" + this.redirect_uri + "&");
		builder.append("grant_type=" + this.grant_type);

		return builder.toString();
	}

}
