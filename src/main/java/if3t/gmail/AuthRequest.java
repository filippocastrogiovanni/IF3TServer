package if3t.gmail;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import if3t.models.User;

public class AuthRequest {

	private String client_id, state, access_type, prompt, 
					scope, response_type, redirect_uri;
	
	
	public AuthRequest(User user) {
		this.client_id = "1087608412755-q2loo7j3fu403k55mmclebf0e6u06e91.apps.googleusercontent.com";
		this.scope = "https://www.googleapis.com/auth/gmail.readonly "
					+ "https://www.googleapis.com/auth/gmail.compose "
					+ "https://www.googleapis.com/auth/gmail.send";
		this.redirect_uri = "http://localhost:8181/gmail/authresponse";
		this.response_type = "code";
		this.state = new BCryptPasswordEncoder().encode(user.getUsername());
		this.prompt = "consent select-account";
		this.access_type = "offline";
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("https://accounts.google.com/o/oauth2/v2/auth?");
		builder.append("response_type=" + this.response_type + "&");
		builder.append("client_id=" + this.client_id + "&");
		builder.append("scope=" + this.scope + "&");
		builder.append("redirect_uri=" + this.redirect_uri + "&");
		builder.append("state=" + this.state + "&");
		builder.append("prompt=" + this.prompt + "&");
		builder.append("access_type=" + this.access_type + "&");
		
		return builder.toString();
	}
	
	
}
