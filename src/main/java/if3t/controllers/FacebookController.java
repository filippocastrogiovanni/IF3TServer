package if3t.controllers;

import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NoPermissionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import if3t.apis.FacebookAuthRequest;
import if3t.apis.FacebookTokenRequest;
import if3t.apis.FacebookTokenResponse;
import if3t.entities.Authorization;
import if3t.entities.Channel;
import if3t.entities.User;
import if3t.exceptions.NotLoggedInException;
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

	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/facebook/auth", method = RequestMethod.GET)
	public Response facebookAuth() throws NotLoggedInException, NoPermissionException {
//		System.out.println("Server has received a token request");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		User loggedUser = userService.getUserByUsername(auth.getName());
		if (loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		Authorization authorization = authService.getAuthorization(loggedUser.getId(), "facebook");
		if(authorization == null){
			FacebookAuthRequest req = new FacebookAuthRequest(loggedUser);
			authRequests.put(req.getState(), loggedUser.getUsername());
			return new Response(req.toString(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
		}
		else{
			return new Response("/facebook/revoke", HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
		}
		
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/facebook/authresponse", method = RequestMethod.GET)
	//public Response facebookAuth() throws NotLoggedInException, NoPermissionException {
	public String facebookAuthResponse(@RequestParam(value = "state", required = false) String state, 
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error)
			throws NotLoggedInException, NoPermissionException, URISyntaxException {
		
//		System.out.println("Server has received a authresponse from Facebook");

		if (error != null) {
			// TODO creare GMAil exception
			return "<h1>ERROR: connection refused</h1>";
		}

		if (state == null)
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		if (!authRequests.containsKey(state))
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		String username = authRequests.get(state);

		User loggedUser = userService.getUserByUsername(username);
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
//		System.out.println(response.getBody());


		if(!facebookRS.isValid())
			return "ERROR";
		else{
			
			channelService.authorizeChannel(loggedUser.getId(), "facebook", facebookRS.getAccess_token(), null,
					facebookRS.getToken_type(), facebookRS.getExpiration_date());

			return "<script>window.close();</script>";
		}
	}
		
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/facebook/revoke", method = RequestMethod.GET)
	public Response facebookRevokeAuth() throws NotLoggedInException, NoPermissionException {
		//System.out.println("Server has received a token revocation request");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		User loggedUser = userService.getUserByUsername(auth.getName());
		if (loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		Authorization facebookAuth = authService.getAuthorization(loggedUser.getId(), "facebook");

		/*
		DefaultLegacyFacebookClient oldRestAPIFbClient = new DefaultLegacyFacebookClient(facebookAuth.getAccessToken()); 
		FacebookClient fbClient = new DefaultFacebookClient(facebookAuth.getAccessToken());
		com.restfb.types.User me = fbClient.fetchObject("me", com.restfb.types.User.class);
		String facebookUserID = me.getId();
		oldRestAPIFbClient.execute("auth_revokeAuthorization"
				, Parameter.with("uid", facebookUserID)
				); 
		*/
		
		/*
		RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(REST_SERVICE_URI+"/user/3");
        */
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/v2.7/me/permissions")
		        .queryParam("access_token", facebookAuth.getAccessToken());

		HttpEntity<?> entity = new HttpEntity<>(headers);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> response = restTemplate.exchange(
		        builder.build().encode().toUri(), 
		        HttpMethod.DELETE, 
		        entity, 
		        String.class);
		
		Channel channel = channelService.findByKeyword("facebook");
   	    Authorization authorization_to_delete = authRepository.findByUserAndChannel(loggedUser, channel);
		authService.deleteAuthorization(authorization_to_delete.getId());
		return new Response("OK", HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
	}
}