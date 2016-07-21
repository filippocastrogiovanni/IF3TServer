package if3t.apis;

import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;
import com.restfb.types.User;

public class FacebookUtil {

	//TRIGGER: NEW POST BY USER
	public static int calculate_new_posts_by_user_number(Long calendar_seconds_to_now, String access_token) throws Exception{
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
			//TODO insert calendar_seconds_to_now as parameter
			"https://graph.facebook.com/v2.7/me/posts?fields=message,type&since=1465854809&access_token="+access_token);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> response = restTemplate.exchange(
		        builder.build().encode().toUri(), 
		        HttpMethod.GET, 
		        entity, 
		        String.class);
		JSONObject json_response = new JSONObject(response.getBody());
		JSONArray data_json_response = (JSONArray)json_response.get("data");
		int new_posts_by_user_number = 0;
		for(int i=0; i<data_json_response.length(); i++){
			JSONObject data_element_json_object = data_json_response.getJSONObject(i);
			if(data_element_json_object.get("type").equals("status")){
				new_posts_by_user_number++; //count only status posts
			}
		}
		
		return new_posts_by_user_number;
	}
	
	//TRIGGER: CHANGE OF FULL NAME
	public static boolean is_full_name_changed(Long calendar_seconds_to_now, String access_token, ConcurrentHashMap<String, String> couples_access_token_full_names)  throws Exception{
		FacebookClient fbClient = new DefaultFacebookClient(access_token);
		User me = fbClient.fetchObject("me", User.class);
		String fetched_full_name = me.getName();
		if(fetched_full_name==null)
			throw new Exception();
		String old_full_name = couples_access_token_full_names.get(access_token);
		couples_access_token_full_names.put(access_token, fetched_full_name);
		if(fetched_full_name.equals(old_full_name)){
			return false;
		}
		else
			return true;
	}
	
	//TRIGGER: CHANGE OF PROFILE PICTURE
	public static boolean is_profile_picture_changed(Long calendar_seconds_to_now, String access_token, ConcurrentHashMap<String, String> couples_access_token_profile_pictures)  throws Exception{
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
			"https://graph.facebook.com/v2.7/me?fields=picture{url}&access_token="+access_token);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> response = restTemplate.exchange(
		        builder.build().encode().toUri(), 
		        HttpMethod.GET, 
		        entity, 
		        String.class);
		JSONObject json_response = new JSONObject(response.getBody());
		JSONObject data_json_response = (JSONObject) ((JSONObject)json_response.get("picture")).get("data");
		String fetched_profile_picture = (String) data_json_response.getString("url");
		if(fetched_profile_picture==null)
			throw new Exception();
		String old_profile_picture = couples_access_token_profile_pictures.get(access_token);
		couples_access_token_profile_pictures.put(access_token, fetched_profile_picture);
		if(fetched_profile_picture.equals(old_profile_picture)){
			return false;
		}
		else
			return true;
	}
	
	//TRIGGER: CHANGE OF LOCATION
	public static boolean is_location_changed(Long calendar_seconds_to_now, String access_token, ConcurrentHashMap<String, String> couples_access_token_locations)  throws Exception{
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
			"https://graph.facebook.com/v2.7/me?fields=location&access_token="+access_token);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> response = restTemplate.exchange(
		        builder.build().encode().toUri(), 
		        HttpMethod.GET, 
		        entity, 
		        String.class);
		JSONObject json_response = new JSONObject(response.getBody());
		JSONObject data_json_response = (JSONObject)json_response.get("location");
		String fetched_location = (String) data_json_response.getString("name");
		if(fetched_location==null)
			throw new Exception();
		String old_location = couples_access_token_locations.get(access_token);
		couples_access_token_locations.put(access_token, fetched_location);
		if(fetched_location.equals(old_location)){
			return false;
		}
		else
			return true;
	}
	
	//ACTION: PUBLISH NEW POST BY USER
	public static String publish_new_post(String content_message, String access_token) throws Exception{
		FacebookClient fbClient = new DefaultFacebookClient(access_token);
		FacebookType publishMessageResponse= fbClient.publish("me/feed", FacebookType.class,
		         Parameter.with("message", content_message));
		return publishMessageResponse.getType();
	}
}
