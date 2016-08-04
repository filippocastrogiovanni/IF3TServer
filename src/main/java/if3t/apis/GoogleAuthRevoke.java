package if3t.apis;

public class GoogleAuthRevoke {

	private String token;

	public GoogleAuthRevoke(String token) {
		super();
		this.token = token;
	}

	public String getToken() {
		return token;
	}
	
	public String getRevokeUrl() {
		return "https://accounts.google.com/o/oauth2/revoke?token="+token;
	}
	
}
