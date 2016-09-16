package if3t.timer;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import if3t.apis.FacebookUtil;
import if3t.apis.GmailUtil;
import if3t.apis.GoogleCalendarUtil;
import if3t.apis.GoogleTokenResponse;
import if3t.exceptions.InvalidParametersException;
import if3t.models.ActionIngredient;
import if3t.models.Authorization;
import if3t.models.Channel;
import if3t.models.ChannelStatus;
import if3t.models.ParametersActions;
import if3t.models.ParametersTriggers;
import if3t.models.Recipe;
import if3t.models.TriggerIngredient;
import if3t.models.User;
import if3t.services.ActionIngredientService;
import if3t.services.AuthorizationService;
import if3t.services.ChannelStatusService;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

@Component
public class ScheduledTasks {

	@Autowired
    private RecipeService recipeService;
	@Autowired
	private TriggerIngredientService triggerIngredientService;
	@Autowired
	private ActionIngredientService actionIngredientService;
	@Autowired
	private AuthorizationService authService;
	@Autowired
	private ChannelStatusService channelStatusService;
	
	//hashmaps with one row for each user who has this trigger
	private ConcurrentHashMap<String, String> couples_access_token_full_names = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> couples_access_token_profile_pictures = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> couples_access_token_locations = new ConcurrentHashMap<String, String>();

    /*
    @Scheduled(fixedRate = 1000*60*5)
    public void facebookScheduler_trigger_new_posts() {	 
   	   Long timestamp_in_seconds = ( Calendar.getInstance().getTimeInMillis() - (1000*60*5) ) / 1000;
   	   int number_new_posts = 0;
   	   //TO DO substitute the following token with the real user one
   	   try {
   		   number_new_posts = FacebookUtil.calculate_new_posts_by_user_number(timestamp_in_seconds, "EAAXpcOQrZCRsBAJEMf1HTTsomV7IHt3SdflvWc8ZC1caBRMvoEtVQmx9e6jPw65gkRT6kLhFkCJNk68BVuerkI3pge5bEfsszXDABP9FZAHFRWYaZCNWeoZCsmLeAnVmkfEzfqHV0GcNdxCWwcOXsMiP5QZB3SiMUZD");
		} catch (Exception e) {
			System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
			e.printStackTrace();
			number_new_posts = -1;
		}	   
    }
    
    @Scheduled(fixedRate = 1000*60*5)
    public void facebookScheduler_trigger_full_name() {	 
   	   Long timestamp_in_seconds = ( Calendar.getInstance().getTimeInMillis() - (1000*60*5) ) / 1000;
   	   //TO DO substitute the following token with the real user one
   	   try {
			boolean changed = FacebookUtil.is_full_name_changed(timestamp_in_seconds, "EAAXpcOQrZCRsBAJEMf1HTTsomV7IHt3SdflvWc8ZC1caBRMvoEtVQmx9e6jPw65gkRT6kLhFkCJNk68BVuerkI3pge5bEfsszXDABP9FZAHFRWYaZCNWeoZCsmLeAnVmkfEzfqHV0GcNdxCWwcOXsMiP5QZB3SiMUZD", couples_access_token_full_names);
		} catch (Exception e) {
			System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
			e.printStackTrace();
		}
    }
    
    @Scheduled(fixedRate = 1000*60*5)
    public void facebookScheduler_trigger_profile_picture() {	
   	   Long timestamp_in_seconds = ( Calendar.getInstance().getTimeInMillis() - (1000*60*5) ) / 1000;
   	   //TO DO substitute the following token with the real user one
   	   try {
			boolean changed = FacebookUtil.is_profile_picture_changed(timestamp_in_seconds, "EAAXpcOQrZCRsBAJEMf1HTTsomV7IHt3SdflvWc8ZC1caBRMvoEtVQmx9e6jPw65gkRT6kLhFkCJNk68BVuerkI3pge5bEfsszXDABP9FZAHFRWYaZCNWeoZCsmLeAnVmkfEzfqHV0GcNdxCWwcOXsMiP5QZB3SiMUZD", couples_access_token_profile_pictures);
		} catch (Exception e) {
			System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
			e.printStackTrace();
		}	
    }
    @Scheduled(fixedRate = 1000*60*5)
    public void facebookScheduler_trigger_location() {	 
   	   Long timestamp_in_seconds = ( Calendar.getInstance().getTimeInMillis() - (1000*60*5) ) / 1000;
   	   //TO DO substitute the following token with the real user one
   	   try {
			boolean changed = FacebookUtil.is_location_changed(timestamp_in_seconds, "EAAXpcOQrZCRsBAJEMf1HTTsomV7IHt3SdflvWc8ZC1caBRMvoEtVQmx9e6jPw65gkRT6kLhFkCJNk68BVuerkI3pge5bEfsszXDABP9FZAHFRWYaZCNWeoZCsmLeAnVmkfEzfqHV0GcNdxCWwcOXsMiP5QZB3SiMUZD", couples_access_token_locations);
		} catch (Exception e) {
			System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
			e.printStackTrace();
		}	 
    }
    @Scheduled(fixedRate = 1000*60*5)
    public void facebookScheduler_action_new_post() {	 
   	   //TO DO substitute the following token with the real user one and the following message with the real one
   	   try {
		String response_type = FacebookUtil.publish_new_post("New post" , "EAAXpcOQrZCRsBAJEMf1HTTsomV7IHt3SdflvWc8ZC1caBRMvoEtVQmx9e6jPw65gkRT6kLhFkCJNk68BVuerkI3pge5bEfsszXDABP9FZAHFRWYaZCNWeoZCsmLeAnVmkfEzfqHV0GcNdxCWwcOXsMiP5QZB3SiMUZD");
		} catch (Exception e) {
			System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
			e.printStackTrace();
		}	 
    }    
   */

	@Scheduled(fixedRate = 1000*60*5)
	public void gmailScheduler(){
		/*TimeZone zone = TimeZone.getTimeZone("GMT-3");
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(1473863892389l);
		c.setTimeZone(zone);
		System.out.println(c.get(Calendar.ZONE_OFFSET));
		try {
			GoogleCalendarUtil.createEvent(c, c, "Prova", "ciao ciao", "Torino");
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidParametersException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		Authorization auth = authService.getAuthorization(7l, "gcalendar");
		try {
			Calendar now = Calendar.getInstance();
			if(auth != null && auth.getExpireDate()*1000 > now.getTimeInMillis()){
				System.out.println("OK");
				GoogleCalendarUtil.isEventAdded(auth);	
			}
			else
				System.out.println("gcalendar auth scaduto");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		RestTemplate restTemplate = new RestTemplate();

		List<Recipe> gmailTriggerRecipes = recipeService.getRecipeByTriggerChannel("gmail");
		for(Recipe recipe: gmailTriggerRecipes){
			User user = recipe.getUser();
			Channel triggerChannel = recipe.getTrigger().getChannel();
			Channel actionChannel = recipe.getAction().getChannel();
			Authorization triggerAuth = authService.getAuthorization(user.getId(), triggerChannel.getKeyword());
			Authorization actionAuth = authService.getAuthorization(user.getId(), actionChannel.getKeyword());
			
			//Checking if the access token of the trigger channel is expired
			Calendar now = Calendar.getInstance();
			if(triggerAuth == null || triggerAuth.getExpireDate()*1000 <= now.getTimeInMillis()){
				System.out.println("scaduto");
				continue;
			}
			
			//Checking if the access token of the action channel is not present
			if(actionAuth == null)
				continue;
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", triggerAuth.getTokenType() + " " + triggerAuth.getAccessToken());
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

			String url = "https://www.googleapis.com/gmail/v1/users/"
					+ "me/messages?"
					+ "q=";

			List<TriggerIngredient> triggerIngredients = triggerIngredientService.getRecipeTriggerIngredients(recipe.getId());
			for(TriggerIngredient triggerIngredient: triggerIngredients){
				ParametersTriggers param = triggerIngredient.getParam();
				url += param.getKeyword() + ":" + triggerIngredient.getValue();
			}
			
			Long timestamp = 0l;
			
			ChannelStatus channelStatus = channelStatusService.readChannelStatus(user.getId(), "gmail");
			if(channelStatus == null)
				timestamp = Calendar.getInstance().getTimeInMillis()- (1000*60*5);
			else
				timestamp = channelStatus.getSinceRef();

			url += " after:" + timestamp/1000;

			HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

			channelStatusService.updateChannelStatus(user.getId(), timestamp + (1000*60*5));
			
			JSONObject obj = new JSONObject(response.getBody());
			try{
				int result = obj.getInt("resultSizeEstimate");
				if(result > 0){  		      		   
					List<ActionIngredient> actionIngredients = actionIngredientService.getRecipeActionIngredients(recipe.getId());
					
					//Checking if the access token of the action channel is expired
					now = Calendar.getInstance();
					if(actionAuth.getExpireDate()*1000 <= now.getTimeInMillis())
						continue;
					
					switch(recipe.getAction().getChannel().getKeyword()){
						case "gmail" :
							JSONArray messages = obj.getJSONArray("messages");
							for(int i=0; i< messages.length(); i++){
								JSONObject message = messages.getJSONObject(i);
								String messageId = message.getString("id");
								String messageUrl = "https://www.googleapis.com/gmail/v1/users/me/messages/" + messageId;
	
								HttpEntity<String> messageResponse = restTemplate.exchange(messageUrl, HttpMethod.GET, entity, String.class);
								
								String to = "";
								String subject = "";
								String body = "";
								
								for(ActionIngredient actionIngredient: actionIngredients){
									ParametersActions param = actionIngredient.getParam();

									if(param.getKeyword().equals("to"))
										to = actionIngredient.getValue();
									if(param.getKeyword().equals("subject"))
										subject = actionIngredient.getValue();
									if(param.getKeyword().equals("body"))
										body = actionIngredient.getValue();
								}
								GmailUtil.sendEmail(to, subject, body, actionAuth);
								//System.out.println(messageResponse.getBody());
							}
							break;
						case "calendar" :
							break;
						case "facebook" :
							String message = "";
							for(ActionIngredient actionIngredient: actionIngredients){
								ParametersActions param = actionIngredient.getParam();
	
								if(param.getKeyword().equals("post"))
									message = actionIngredient.getValue();
							}
							FacebookUtil.publish_new_post(message, actionAuth.getAccessToken());
							break;
						case "twitter" :
							break;
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

	}
	
	
}