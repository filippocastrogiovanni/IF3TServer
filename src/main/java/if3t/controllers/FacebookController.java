package if3t.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NoPermissionException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
import if3t.models.Authorization;
import if3t.models.Channel;
import if3t.models.Response;
import if3t.repositories.AuthorizationRepository;
import if3t.services.AuthorizationService;
import if3t.services.ChannelService;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class FacebookController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private ChannelService channelService;
	
	@Autowired
	private AuthorizationService authService;

	@Autowired
	private AuthorizationRepository authRepository;
	
	private ConcurrentHashMap<String, String> authRequests = new ConcurrentHashMap<String, String>();

	@RequestMapping(value = "/facebook/auth", method = RequestMethod.GET)
	public Response facebookAuth() throws NotLoggedInException, NoPermissionException {
		System.out.println("Server has received a token request");

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
		return new Response(req.toString(), HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
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
		////RestTemplate restTemplate = new RestTemplate();
		////String response = restTemplate.getForObject(new URI("https://graph.facebook.com/v2.3/oauth/access_token?redirect_uri=http://localhost:8181/facebook/authresponse&client_id=1664045957250331"), String.class);
			
		//String response = restTemplate.getForObject(new URI(facebookRQ.getToken_uri()+"?"+facebookRQ.getRequestBody()), String.class);
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(facebookRQ.getToken_uri())
		        .queryParam("client_id", facebookRQ.getClient_id())
		        .queryParam("client_secret", facebookRQ.getClient_secret())
		        .queryParam("code", facebookRQ.getCode())
		        .queryParam("redirect_uri", facebookRQ.getRedirect_uri());

		HttpEntity<?> entity = new HttpEntity<>(headers);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> response = restTemplate.exchange(
		        builder.build().encode().toUri(), 
		        HttpMethod.GET, 
		        entity, 
		        String.class);
		FacebookTokenResponse facebookRS = new FacebookTokenResponse(response.getBody());
		System.out.println(response.getBody());


		if(!facebookRS.isValid())
			return "ERROR";
		else{
			
			channelService.authorizeChannel(loggedUser.getId(), "facebook", facebookRS.getAccess_token(), null,
					facebookRS.getToken_type(), facebookRS.getExpiration_date());

			return "<script>window.close();</script>";
		}
	}
		
	@RequestMapping(value = "/facebook/revokeauth", method = RequestMethod.GET)
	public Response facebookRevokeAuth() throws NotLoggedInException, NoPermissionException {
		System.out.println("Server has received a token revocation request");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		if3t.models.User loggedUser = userService.getUserByUsername(auth.getName());
		if (loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		Channel channel = channelService.findByKeyword("facebook");
   	    Authorization authorization_to_delete = authRepository.findByUserAndChannel(loggedUser, channel);
		authService.deleteAuthorization(authorization_to_delete.getId());
		return new Response("OK", HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
	}

}
