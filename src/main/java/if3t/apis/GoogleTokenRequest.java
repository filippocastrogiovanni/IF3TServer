package if3t.apis;

import java.io.Serializable;

public class GoogleTokenRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	String code;			//The authorization code returned from the initial request.
	String client_id;		//The client ID obtained from the API Console.
	String client_secret;	//The client secret obtained from the API Console.
	String redirect_uri;	//One of the redirect URIs listed for this project in the API Console.
	String grant_type;		//As defined in the OAuth 2.0 specification, this field must contain a value of authorization_code.
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public String getClient_secret() {
		return client_secret;
	}
	public void setClient_secret(String client_secret) {
		this.client_secret = client_secret;
	}
	public String getRedirect_uri() {
		return redirect_uri;
	}
	public void setRedirect_uri(String redirect_uri) {
		this.redirect_uri = redirect_uri;
	}
	public String getGrant_type() {
		return grant_type;
	}
	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}
}
