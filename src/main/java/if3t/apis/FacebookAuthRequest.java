package if3t.apis;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import if3t.entities.User;

public class FacebookAuthRequest {

	private String client_id, state, 
	scope, response_type, redirect_uri;


	public FacebookAuthRequest(User user) {
		this.client_id = "1664045957250331";
		this.response_type = "code";
		this.redirect_uri = "http://localhost:8181/facebook/authresponse";
		this.state = new BCryptPasswordEncoder().encode(user.getUsername());
		/* Response type può essere:
		   -code (default). I dati della risposta sono parametri di URL contenenti il parametro code (una stringa criptata diversa per ogni richiesta di accesso). Si tratta del comportamento predefinito quando il parametro non viene specificato. È utile per la gestione del token da parte del server.
		   -token. I dati della risposta sono un frammento di URL contenente un token d'accesso. Le app per computer devono usare questa impostazione per response_type. È utile per la gestione del token da parte del client.
		 */
		this.scope = "public_profile,user_friends,email,user_about_me,user_birthday,user_likes,user_location,user_photos,user_posts,publish_actions";
	}
		
	public String getState() {
		return state;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("https://www.facebook.com/dialog/oauth?");
		builder.append("client_id=" + this.client_id + "&");
		builder.append("display=popup&");
		builder.append("response_type=" + this.response_type + "&");
		builder.append("scope=" + this.scope + "&");
		builder.append("redirect_uri=" + this.redirect_uri + "&");
		builder.append("state=" + this.state + "&");
		
		return builder.toString();
	}

}
