package if3t.apis;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import if3t.entities.User;

public class GoogleAuthRequest {

	private String client_id, state, access_type, prompt, 
					scope, response_type, redirect_uri;
	
	
	public GoogleAuthRequest(User user) {
		this.client_id = "205247608184-qn9jd5afpqai7n8n6hbhb2qgvad7mih8.apps.googleusercontent.com";
		this.scope = "https://www.googleapis.com/auth/gmail.readonly "
					+ "https://www.googleapis.com/auth/gmail.compose "
					+ "https://www.googleapis.com/auth/gmail.send "
					+ "https://www.googleapis.com/auth/gmail.modify";
		this.redirect_uri = "http://localhost:8181/gmail/authresponse";
		this.response_type = "code";
		this.state = new BCryptPasswordEncoder().encode(user.getUsername());
		this.prompt = "consent select_account";
		this.access_type = "offline";
	}
	
	

	public String getState() {
		return state;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("https://accounts.google.com/o/oauth2/auth?");
		builder.append("response_type=" + this.response_type + "&");
		builder.append("client_id=" + this.client_id + "&");
		builder.append("scope=" + this.scope + "&");
		builder.append("redirect_uri=" + this.redirect_uri + "&");
		builder.append("state=" + this.state + "&");
		builder.append("prompt=" + this.prompt + "&");
		builder.append("access_type=" + this.access_type);
		
		return builder.toString();
	}
	
	
}
