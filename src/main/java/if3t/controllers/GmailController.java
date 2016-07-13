package if3t.controllers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NoPermissionException;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.hibernate.engine.transaction.jta.platform.internal.SynchronizationRegistryBasedSynchronizationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import if3t.apis.GoogleAuthRequest;
import if3t.apis.GoogleTokenRequest;
import if3t.apis.GoogleTokenResponse;
import if3t.exceptions.NotLoggedInException;
import if3t.models.Response;
import if3t.models.User;
import if3t.services.ChannelService;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class GmailController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private ChannelService channelService;
	
	private ConcurrentHashMap<String, String> authRequests = new ConcurrentHashMap<String, String>();

	@RequestMapping(value = "/gmail/auth", method = RequestMethod.GET)
	public Response gmailAuth() throws NotLoggedInException, NoPermissionException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		User loggedUser = userService.getUserByUsername(auth.getName());
		if (loggedUser == null)
			throw new NotLoggedInException("ERROR: not logged in!");

		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		GoogleAuthRequest req = new GoogleAuthRequest(loggedUser);
		authRequests.put(req.getState(), loggedUser.getUsername());
		return new Response(req.toString(), 200);
	}

	@RequestMapping(value = "/gmail/authresponse", method = RequestMethod.GET)
	public String gmailAuthResponse(@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error)
			throws NotLoggedInException, NoPermissionException, URISyntaxException {

		if (error != null) {
			// TODO creare GMAil exception
			return "<h1>ERROR: connection refused</h1>";
		}
		
		if (state == null)
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		
		if(!authRequests.containsKey(state))
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		
		String username = authRequests.get(state);
		authRequests.remove(state);
		User loggedUser = userService.getUserByUsername(username);
		if (loggedUser == null)
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");

		if (!loggedUser.isEnabled())
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		
		if(code == null)
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////
		String tokenUrl = "https://accounts.google.com/o/oauth2/token";
		System.out.println("codice: "+code);
		/*
		RestTemplate restTemplate = new RestTemplate();
		FormHttpMessageConverter converter = new FormHttpMessageConverter();
        MediaType mediaType = new MediaType("application","x-www-form-urlencoded", Charset.forName("UTF-8"));
        converter.setSupportedMediaTypes(Arrays.asList(mediaType));
		restTemplate.getMessageConverters().add(0, converter);
		
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
		requestBody.add("code",code);
		requestBody.add("client_id","205247608184-qn9jd5afpqai7n8n6hbhb2qgvad7mih8.apps.googleusercontent.com");
		requestBody.add("client_secret","DPPiyrVcd-uqUMw7ponxFKv1");
		requestBody.add("redirect_uri","http://localhost:8181/gmail/tokenresponse");
		requestBody.add("grant_type","authorization_code");
		
		RequestEntity<MultiValueMap<String, String>> request = RequestEntity
				.post(new URI(tokenUrl))
				.body(requestBody);
		ResponseEntity<String> response = restTemplate.exchange(request, String.class);
		*/
		
		try {
		URL obj = new URL(tokenUrl);
		
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		//con.setRequestProperty("User-Agent", USER_AGENT);
		//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		String urlParameters = "code="+code+"&"
				+ "client_id=205247608184-qn9jd5afpqai7n8n6hbhb2qgvad7mih8.apps.googleusercontent.com&"
				+ "client_secret=DPPiyrVcd-uqUMw7ponxFKv1&"
				+ "redirect_uri=http://localhost:8181/gmail/tokenresponse&"
				+ "grant_type=authorization_code";
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		//print result
		System.out.println(response.toString());
		
		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		/*
		RestTemplate restTemplate = new RestTemplate();
		FormHttpMessageConverter converter = new FormHttpMessageConverter();
        MediaType mediaType = new MediaType("application","x-www-form-urlencoded", Charset.forName("UTF-8"));
        converter.setSupportedMediaTypes(Arrays.asList(mediaType));
		restTemplate.getMessageConverters().add(0, converter);
		
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
		requestBody.add("code",code);
		requestBody.add("client_id","1087608412755-q2loo7j3fu403k55mmclebf0e6u06e91.apps.googleusercontent.com");
		requestBody.add("client_secret","DkbqPB6Wh0mD1zuvuwqsWjeY");
		requestBody.add("redirect_uri","http://localhost:8181/gmail/tokenresponse");
		requestBody.add("grant_type","authorization_code");
		System.out.println("codice: "+code);
		RequestEntity<MultiValueMap<String, String>> request = RequestEntity
				.post(new URI(tokenUrl))
				.body(requestBody);

		ResponseEntity<String> response = restTemplate.exchange(request, String.class);
		
		/*HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		requestHeaders.add("Accept","application/json;charset=utf-8");
		
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, requestHeaders);
		ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);
		String result = response.getBody();*/
		return code;
		
		
		
		/*
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
		
        
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("code", code);
		map.add("client_id", "1087608412755-q2loo7j3fu403k55mmclebf0e6u06e91.apps.googleusercontent.com");
		map.add("client_secret", "DkbqPB6Wh0mD1zuvuwqsWjeY");
		map.add("redirect_uri", "http://localhost:8181/gmail/tokenresponse");
		map.add("grant_type", "authorization_code");
		
		User returns = restTemplate.postForObject(tokenUrl, u, User.class, map);
		*/
		
		
		/*HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(2000);
        factory.setConnectTimeout(2000);
		ClientHttpRequestFactory requestFactory = factory;
		*/
		
		/*

		HttpMessageConverter<MultiValueMap<String, ?>> formHttpMessageConverter = new FormHttpMessageConverter();
		HttpMessageConverter<String> stringHttpMessageConverternew = new StringHttpMessageConverter();
		restTemplate.setMessageConverters(Arrays.asList(formHttpMessageConverter, stringHttpMessageConverternew));
		
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
		requestBody.add("code",code);
		requestBody.add("client_id","1087608412755-q2loo7j3fu403k55mmclebf0e6u06e91.apps.googleusercontent.com");
		requestBody.add("client_secret","DkbqPB6Wh0mD1zuvuwqsWjeY");
		requestBody.add("redirect_uri","http://localhost:8181/gmail/tokenresponse");
		requestBody.add("grant_type","authorization_code");
		/*
		GoogleTokenRequest requestBody = new GoogleTokenRequest();
		requestBody.setCode(code);
		requestBody.setClient_id("1087608412755-q2loo7j3fu403k55mmclebf0e6u06e91.apps.googleusercontent.com");
		requestBody.setClient_secret("DkbqPB6Wh0mD1zuvuwqsWjeY");
		requestBody.setRedirect_uri("http://localhost:8181/gmail/tokenresponse");
		requestBody.setGrant_type("authorization_code");
		*/
		/*
		RequestEntity<MultiValueMap<String, String>> request = RequestEntity
				.post(new URI(tokenUrl))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(requestBody);
		
		ResponseEntity<String> response = restTemplate.exchange(request, String.class);
				
		//GoogleTokenResponse responseBody = response.getBody();
		//System.out.println("response "+responseBody.getAccess_token());
		
		
		
		
		
		/*HttpMessageConverter<?> formHttpMessageConverter = new FormHttpMessageConverter();
		HttpMessageConverter<?> stringHttpMessageConverternew = new StringHttpMessageConverter();
		restTemplate.setMessageConverters(Arrays.asList(formHttpMessageConverter, stringHttpMessageConverternew));*/
		

		/*
		RequestEntity<MultiValueMap<String, String>> request = RequestEntity
														.post(new URI(tokenUrl))
		 												.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		 												.body(map);
		*/
		//System.out.println("header:" + request.getHeaders());
		
		//ResponseEntity<String> response = restTemplate.exchange(request, String.class);
 /*
		String body = response.getBody();
		MediaType contentType = response.getHeaders().getContentType();
		HttpStatus statusCode = response.getStatusCode();
		*/
		
		/*
        Calendar now = Calendar.getInstance();
        GoogleTokenResponse resp = response.getBody();
		channelService.authorizeChannel(loggedUser.getId(), "gmail", 
				resp.getAccess_token(), 
				resp.getRefresh_token(),
				resp.getToken_type(),
				now.getTimeInMillis()/1000+resp.getExpires_in()-1);
		*/
        
		//return "ciao";
	}
	
	@RequestMapping(value = "/gmail/tokenresponse", method = RequestMethod.GET)
	public String gmailTokenResponse() {
		System.out.println("google ha risposto");
		return null;
	}
	

}
