package if3t.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NoPermissionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import if3t.apis.GCalendarAuthRequest;
import if3t.apis.GoogleTokenRequest;
import if3t.apis.GoogleTokenResponse;
import if3t.exceptions.NotLoggedInException;
import if3t.models.Authorization;
import if3t.models.Response;
import if3t.models.User;
import if3t.services.AuthorizationService;
import if3t.services.ChannelService;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class GoogleCalendarController {

	@Autowired
	private UserService userService;

	@Autowired
	private ChannelService channelService;
	
	@Autowired
	private AuthorizationService authService;
	
	private ConcurrentHashMap<String, String> authRequests = new ConcurrentHashMap<String, String>();

	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/gcalendar/auth", method = RequestMethod.GET)
	public Response gCalendarAuth() throws NotLoggedInException, NoPermissionException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		User loggedUser = userService.getUserByUsername(auth.getName());
		if (loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		Authorization authorization = authService.getAuthorization(loggedUser.getId(), "gcalendar");
		if(authorization == null){
			GCalendarAuthRequest req = new GCalendarAuthRequest(loggedUser);
			authRequests.put(req.getState(), loggedUser.getUsername());
			return new Response(req.toString(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
		}
		else{
			return new Response("/gcalendar/revoke", HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
		}
		
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/gcalendar/revoke", method = RequestMethod.GET)
	public Response gCalendarRevoke() throws NotLoggedInException, NoPermissionException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		User loggedUser = userService.getUserByUsername(auth.getName());
		if (loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		Authorization authorization = authService.getAuthorization(loggedUser.getId(), "gcalendar");
		authService.deleteAuthorization(authorization.getId());
		return new Response("OK", HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
	}

	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/gcalendar/authresponse", method = RequestMethod.GET)
	public String gCalendarAuthResponse(@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error)
			throws NotLoggedInException, NoPermissionException, URISyntaxException {

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

		authRequests.remove(state);
		
		GoogleTokenRequest googleRQ = new GoogleTokenRequest(code, "http://localhost:8181/gcalendar/authresponse");
		RestTemplate restTemplate = new RestTemplate();
		MediaType mediaType = new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8"));
		RequestEntity<String> request = RequestEntity.post(new URI(googleRQ.getToken_uri()))
				.contentLength(googleRQ.getRequestBody().getBytes().length).contentType(mediaType)
				.body(googleRQ.getRequestBody());

		ResponseEntity<String> response = restTemplate.exchange(request, String.class);
		GoogleTokenResponse googleRS = new GoogleTokenResponse(response.getBody());

		if(!googleRS.isValid())
			return "ERROR";
		
		channelService.authorizeChannel(loggedUser.getId(), "gcalendar", googleRS.getAccess_token(), googleRS.getRefresh_token(),
				googleRS.getToken_type(), googleRS.getExpiration_date());


		return "<script>window.close();</script>";
	}
}