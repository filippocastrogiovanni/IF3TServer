package if3t.apis;

import java.io.Serializable;

public class FacebookTokenRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6617761087201756156L;
	/**
	 * 
	 */
	private String token_uri;
	private String client_id; // The client ID obtained from the API Console.
	private String redirect_uri; // One of the redirect URIs listed for this project in the API Console.
	private String client_secret; // The client secret obtained from the API Console.
	private String code; // The authorization code returned from the initial request.	

	public FacebookTokenRequest(String code) {
		super();
		this.token_uri = "https://graph.facebook.com/v2.3/oauth/access_token?";
		this.client_id = "1664045957250331";
		this.redirect_uri = "http://localhost:8181/facebook/authresponse";
		this.client_secret = "4489952bdf1294fe0fd156288a2602f4";
		this.code = code;
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

	public String getRequestBody() {
		StringBuilder builder = new StringBuilder();
		builder.append("client_id=" + this.client_id + "&");
		builder.append("redirect_uri=" + this.redirect_uri + "&");
		builder.append("client_secret=" + this.client_secret + "&");
		builder.append("code=" + this.code + "&");

		return builder.toString();
	}

}
