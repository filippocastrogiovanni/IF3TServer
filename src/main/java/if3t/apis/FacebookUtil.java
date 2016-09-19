package if3t.apis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;
import com.restfb.types.User;

import if3t.entities.ChannelStatus;
import if3t.services.ChannelStatusService;

@Component
public class FacebookUtil {

	@Autowired
	private ChannelStatusService channelStatusService;	
	@Value("${app.scheduler.value}")
	private long fixedRateString;
	
	//TRIGGER: NEW POST BY USER
	public ArrayList<String> calculate_new_posts_by_user_number(String access_token, Long recipe_id) throws Exception{
		ArrayList<String> new_posts = new ArrayList<String>();
		ChannelStatus css = channelStatusService.readChannelStatusByRecipeId(recipe_id);
		if(css == null){
			Long timestamp = ( Calendar.getInstance().getTimeInMillis() - (fixedRateString) ) / 1000;
			channelStatusService.createNewChannelStatus(recipe_id, timestamp);
		}
		Long since_ref = css.getFacebookSinceRef();
		ChannelStatus old_cs = channelStatusService.readChannelStatusByRecipeId(recipe_id);
		if(since_ref == null || since_ref == 0){
			since_ref = css.getSinceRef();
			if(since_ref == null || since_ref == 0){
				since_ref = ( Calendar.getInstance().getTimeInMillis() - (fixedRateString) ) / 1000;
				old_cs.setSinceRef(since_ref);
			}		
		}
		else{
			since_ref = ( Calendar.getInstance().getTimeInMillis() - (fixedRateString) ) / 1000;
			old_cs.setFacebookSinceRef(since_ref);	
			channelStatusService.updateChannelStatus(old_cs);			
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		/*
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
			"https://graph.facebook.com/v2.7/me/posts?fields=message,type&since="+ since_ref + "&access_token="+access_token);
		*/
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
			 "https://graph.facebook.com/v2.7/me/posts?fields=message,type&since=1470000000&access_token="+access_token);

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
			if(data_element_json_object.get("type").equals("status") && data_element_json_object.get("message")!=null && !data_element_json_object.get("message").equals("")){
				new_posts.add((String) data_element_json_object.get("message")); //count only status posts
			}
		}
		
		return new_posts;
	}
	
	//TRIGGER: CHANGE OF FULL NAME
	public boolean is_full_name_changed(String access_token, ConcurrentHashMap<String, String> couples_access_token_full_names, Long recipe_id)  throws Exception{
		FacebookClient fbClient = new DefaultFacebookClient(access_token);
		User me = fbClient.fetchObject("me", User.class);
		String fetched_full_name = me.getName();
		if(fetched_full_name==null)
			throw new Exception();
		String old_full_name = couples_access_token_full_names.get(access_token);
		if(old_full_name == null){ //it happens the first time this scheduled task is run
			ChannelStatus css = channelStatusService.readChannelStatusByRecipeId(recipe_id);
			if(css == null){
				Long timestamp = ( Calendar.getInstance().getTimeInMillis() - (fixedRateString) ) / 1000;
				channelStatusService.createNewChannelStatus(recipe_id, timestamp);
				css = channelStatusService.readChannelStatusByRecipeId(recipe_id);
			}
			old_full_name = css.getFacebookFullName();
			couples_access_token_full_names.put(access_token, fetched_full_name);
			if(old_full_name == null){ //still null, means we did not know before, so we skip this time the triggering
				css.setFacebookFullName(fetched_full_name);
				channelStatusService.updateChannelStatus(css);
				return false;
			}
		}
		if(fetched_full_name.equals(old_full_name)){
			return false;
		}
		else{
			couples_access_token_full_names.put(access_token, fetched_full_name);		
			ChannelStatus old_cs = channelStatusService.readChannelStatusByRecipeId(recipe_id);
			old_cs.setFacebookFullName(fetched_full_name);
			channelStatusService.updateChannelStatus(old_cs);
			return true;
		}
	}
	
	//TRIGGER: CHANGE OF PROFILE PICTURE
	public boolean is_profile_picture_changed(String access_token, ConcurrentHashMap<String, String> couples_access_token_profile_pictures, Long recipe_id)  throws Exception{
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
		if(old_profile_picture == null){ //it happens the first time this scheduled task is run
			ChannelStatus css = channelStatusService.readChannelStatusByRecipeId(recipe_id);
			if(css == null){
				Long timestamp = ( Calendar.getInstance().getTimeInMillis() - (fixedRateString) ) / 1000;
				channelStatusService.createNewChannelStatus(recipe_id, timestamp);
				css = channelStatusService.readChannelStatusByRecipeId(recipe_id);
			}
			old_profile_picture = css.getFacebookProfilePicture();
			couples_access_token_profile_pictures.put(access_token, fetched_profile_picture);
			if(old_profile_picture == null){ //still null, means we did not know before, so we skip this time the triggering
				css.setFacebookProfilePicture(fetched_profile_picture);
				channelStatusService.updateChannelStatus(css);
				return false;
			}
		}
		if(fetched_profile_picture.equals(old_profile_picture)){
			return false;
		}
		else{
			couples_access_token_profile_pictures.put(access_token, fetched_profile_picture);	
			ChannelStatus old_cs = channelStatusService.readChannelStatusByRecipeId(recipe_id);
			old_cs.setFacebookProfilePicture(fetched_profile_picture);
			channelStatusService.updateChannelStatus(old_cs);
			return true;
		}
	}
	
	//TRIGGER: CHANGE OF LOCATION
	public boolean is_location_changed(String access_token, ConcurrentHashMap<String, String> couples_access_token_locations, Long recipe_id)  throws Exception{
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
		if(old_location == null){ //it happens the first time this scheduled task is run
			ChannelStatus css = channelStatusService.readChannelStatusByRecipeId(recipe_id);
			if(css == null){
				Long timestamp = ( Calendar.getInstance().getTimeInMillis() - (fixedRateString) ) / 1000;
				channelStatusService.createNewChannelStatus(recipe_id, timestamp);
				css = channelStatusService.readChannelStatusByRecipeId(recipe_id);
			}
			old_location = css.getFacebookLocation();
			couples_access_token_locations.put(access_token, fetched_location);
			if(old_location == null){ //still null, means we did not know before, so we skip this time the triggering
				css.setFacebookLocation(fetched_location);
				channelStatusService.updateChannelStatus(css);
				return false;
			}
		}
		if(fetched_location.equals(old_location)){
			return false;
		}
		else{
			couples_access_token_locations.put(access_token, fetched_location);	
			ChannelStatus old_cs = channelStatusService.readChannelStatusByRecipeId(recipe_id);
			old_cs.setFacebookLocation(fetched_location);
			channelStatusService.updateChannelStatus(old_cs);
			return true;
		}
	}
	
	//ACTION: PUBLISH NEW POST BY USER
	public String publish_new_post(String content_message, String access_token) throws Exception{
		FacebookClient fbClient = new DefaultFacebookClient(access_token);
		FacebookType publishMessageResponse= fbClient.publish("me/feed", FacebookType.class,
		         Parameter.with("message", content_message));
		return publishMessageResponse.getType();
	}
}
