package if3t.controllers;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NoPermissionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.http.javanet.NetHttpTransport;

import if3t.apis.TwitterTemporaryToken;
import if3t.apis.TwitterUtil;
import if3t.exceptions.NotLoggedInException;
import if3t.models.Response;
import if3t.models.User;
import if3t.services.ChannelService;
import if3t.services.UserService;

@CrossOrigin
@RestController
public class TwitterController 
{
	@Autowired
	private UserService userService;
	@Autowired
	private ChannelService channelService;
	private static final String CONSUMER_KEY = "rLWBxF1x5DwCgMhtFzGckQytZ";
    private static final String CONSUMER_SECRET = "HYAWanoKCvBHTdw7hSjMj8LPvpbwJ2MPCADgTEuhubbgTXGDW2";
    private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authenticate";
    private static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
    private ConcurrentHashMap<String, TwitterTemporaryToken> tempTokens = new ConcurrentHashMap<String, TwitterTemporaryToken>();

	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/twitter/auth", method = RequestMethod.GET)
	public Response twitterAuth() throws NotLoggedInException, NoPermissionException, IOException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not logged in!");
		}

		User loggedUser = userService.getUserByUsername(auth.getName());

    	OAuthHmacSigner signer = new OAuthHmacSigner();
    	signer.clientSharedSecret = CONSUMER_SECRET;
      
    	// Step 1: Get a request token
    	OAuthGetTemporaryToken temporaryToken = new OAuthGetTemporaryToken(REQUEST_TOKEN_URL);
    	temporaryToken.consumerKey = CONSUMER_KEY;
    	temporaryToken.transport = new NetHttpTransport();
    	temporaryToken.signer = signer;
    	temporaryToken.callback = "http://localhost:8181/twitter/authresponse";
    	
    	OAuthCredentialsResponse tempTokenResponse = temporaryToken.execute();
    	
    	if (!tempTokenResponse.callbackConfirmed) {
    		throw new RuntimeException();
    	}
      	
    	// Step 2: User grants access
    	OAuthAuthorizeTemporaryTokenUrl authorizeUrl = new OAuthAuthorizeTemporaryTokenUrl(AUTHORIZE_URL);
    	authorizeUrl.temporaryToken = tempTokenResponse.token;
    	tempTokens.put(loggedUser.getUsername(), new TwitterTemporaryToken(tempTokenResponse.token, tempTokenResponse.tokenSecret));
    	
    	return new Response(authorizeUrl.build(), HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/twitter/authresponse", method = RequestMethod.GET)
	public String twitterAuthResponse(@RequestParam String oauth_token, @RequestParam String oauth_verifier) throws NotLoggedInException, NoPermissionException, IOException
	{		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not logged in!");
		}

		User loggedUser = userService.getUserByUsername(auth.getName());
		
		if (!tempTokens.containsKey(loggedUser.getUsername())) {
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		}
		
		if (!tempTokens.get(loggedUser.getUsername()).getToken().equals(oauth_token)) {
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		}
		
		// Step 3: Request the access token the user has approved
    	OAuthHmacSigner signer = new OAuthHmacSigner();
    	signer.clientSharedSecret = CONSUMER_SECRET;
    	signer.tokenSharedSecret = tempTokens.get(loggedUser.getUsername()).getSecret();
    	
    	// Delete tempToken from the HashMap
    	tempTokens.remove(loggedUser.getUsername());
		
    	OAuthGetAccessToken accessToken = new OAuthGetAccessToken(ACCESS_TOKEN_URL);
    	accessToken.consumerKey = CONSUMER_KEY;
    	accessToken.signer = signer;
    	accessToken.transport = new NetHttpTransport();
    	accessToken.temporaryToken = oauth_token;
    	accessToken.verifier = oauth_verifier;
    	
    	OAuthCredentialsResponse accTokenResponse = accessToken.execute();
    	
    	// Step 4: Save the access token into DB
    	// WARNING: refresh token doesn't exist in Twitter, so the saved string is the token secret, that is necessary for the signer
    	channelService.authorizeChannel(loggedUser.getId(), "twitter", accTokenResponse.token, accTokenResponse.tokenSecret, "Access", null);
    	
    	//TwitterUtil.postTweet(loggedUser.getId(), accTokenResponse, "prova riuscita [" + System.currentTimeMillis() + "]", null);
		//new TwitterUtil().printStatuses(loggedUser.getId(), accTokenResponse);
    	
    	return "<script>window.close();</script>";
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/twitter/revokeauth", method = RequestMethod.GET)
	public Response twitterRevokeAuth() throws NotLoggedInException, NoPermissionException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not logged in!");
		}
		
		//TODO manca l'implementazione
		
		return new Response("Channel has been disconnected successfully", HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
	}
}