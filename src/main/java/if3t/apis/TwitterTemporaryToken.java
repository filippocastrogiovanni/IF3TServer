package if3t.apis;

public class TwitterTemporaryToken 
{
	private String token;
	private String secret;
	
	public TwitterTemporaryToken(String token, String secret)
	{
		this.token = token;
		this.secret = secret;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
}