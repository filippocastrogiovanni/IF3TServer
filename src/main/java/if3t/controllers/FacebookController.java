package if3t.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NoPermissionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.types.Post;
import com.restfb.types.User;

import if3t.apis.FacebookAuthRequest;
import if3t.apis.FacebookTokenRequest;
import if3t.apis.FacebookTokenResponse;
import if3t.apis.GoogleAuthRequest;
import if3t.apis.GoogleTokenRequest;
import if3t.apis.GoogleTokenResponse;
import if3t.exceptions.NotLoggedInException;
import if3t.models.Response;
import if3t.services.ChannelService;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class FacebookController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private ChannelService channelService;
	
	private ConcurrentHashMap<String, String> authRequests = new ConcurrentHashMap<String, String>();

	@RequestMapping(value = "/facebook/auth", method = RequestMethod.GET)
	public Response facebookAuth() throws NotLoggedInException, NoPermissionException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		if3t.models.User loggedUser = userService.getUserByUsername(auth.getName());
		if (loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		FacebookAuthRequest req = new FacebookAuthRequest(loggedUser);
		authRequests.put(req.getState(), loggedUser.getUsername());
		return new Response(req.toString(), 200);
	}
	
	@RequestMapping(value = "/facebook/authresponse", method = RequestMethod.GET)
	//public Response facebookAuth() throws NotLoggedInException, NoPermissionException {
	public String facebookAuthResponse(@RequestParam(value = "state", required = false) String state, 
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error)
			throws NotLoggedInException, NoPermissionException, URISyntaxException {
		
		System.out.println("Server has received a authresponse from Facebook");

		if (error != null) {
			// TODO creare GMAil exception
			return "<h1>ERROR: connection refused</h1>";
		}

		if (state == null)
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		if (!authRequests.containsKey(state))
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		String username = authRequests.get(state);

		if3t.models.User loggedUser = userService.getUserByUsername(username);
		if (loggedUser == null)
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		if (code == null)
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		FacebookTokenRequest facebookRQ = new FacebookTokenRequest(code);
		RestTemplate restTemplate = new RestTemplate();
		String response = restTemplate.getForObject(new URI(facebookRQ.getToken_uri()+"?"+facebookRQ.getRequestBody()), String.class);

		FacebookTokenResponse facebookRS = new FacebookTokenResponse(response);

		if(!facebookRS.isValid())
			return "ERROR";
		else{
			channelService.authorizeChannel(loggedUser.getId(), "facebook", facebookRS.getAccess_token(), null,
					facebookRS.getToken_type(), facebookRS.getExpiration_date());

			return "<script>window.close();</script>";
		}
	}
		
		/*
		//do some stuff
		String accessToken = "EAAXpcOQrZCRsBAFpU1GEZCeEdVZAlIZBdWS3CH1dmepoOfzNkqf3SBWJwksOwdvQRY19umTbXQJnwkXfChiMwxhL3zqjPqZAm7kBvZCcTLwZBowQyX0zTwvrhkAk27witQ4p1dyDfqGike47y4AeAZCCVDtnknZBrhTX4bXPpp91JnQZDZD";
		FacebookClient fbClient = new DefaultFacebookClient(accessToken);
		//extending token 
		AccessToken exAccessToken = fbClient.obtainExtendedAccessToken("1664045957250331", "4489952bdf1294fe0fd156288a2602f4"); //appId, appSecret
		System.out.println("The token access " + exAccessToken.getAccessToken() + " expires on" + exAccessToken.getExpires());
		//getting user information
		User me = fbClient.fetchObject("me", User.class);
		System.out.println(me.getFirstName() + " " + me.getBirthdayAsDate().toString());
		//get collection of allpages containing posts
		int posts_counter = 0;
		Connection<Post> result = fbClient.fetchConnection("me/feed", Post.class);
		for(List<Post> page : result){
			for(Post aPost : page){
				System.out.println("POST ID at www.facebook.com/"+ aPost.getId() + " " + aPost.getMessage());
				posts_counter++;
			}
		}
		*/

}
