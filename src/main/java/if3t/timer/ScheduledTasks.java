package if3t.timer;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
import if3t.apis.GoogleTokenResponse;
import if3t.models.ActionIngredient;
import if3t.models.Authorization;
import if3t.models.Channel;
import if3t.models.ParametersActions;
import if3t.models.ParametersTriggers;
import if3t.models.Recipe;
import if3t.models.TriggerIngredient;
import if3t.models.User;
import if3t.services.ActionIngredientService;
import if3t.services.AuthorizationService;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;

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
   
      @Scheduled(fixedRate = 1000*60*5)
    public void gmailScheduler() {	 
    	
	   RestTemplate restTemplate = new RestTemplate();
	   
       List<Recipe> gmailTriggerRecipes = recipeService.getRecipeByTriggerChannel("gmail");
       for(Recipe recipe: gmailTriggerRecipes){
    	   User user = recipe.getUser();
    	   Channel triggerChannel = recipe.getTrigger().getChannel();
    	   Authorization auth = authService.getAuthorization(user.getId(), triggerChannel.getChannelId());
    	   
    	   HttpHeaders headers = new HttpHeaders();
    	   headers.set("Authorization", auth.getTokenType() + " " + auth.getAccessToken());
    	   HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
    	   
    	   String url = "https://www.googleapis.com/gmail/v1/users/"
     				+ "me/messages?"
     				+ "q=";
    	   
    	   
    	   List<TriggerIngredient> triggerIngredients = triggerIngredientService.getRecipeTriggerIngredients(recipe.getId());
    	   for(TriggerIngredient triggerIngredient: triggerIngredients){
    		   ParametersTriggers param = triggerIngredient.getParam();
    		   
    		   url += param.getKeyword() + ":" + triggerIngredient.getValue();
    	   }
    	   Long timestamp = Calendar.getInstance().getTimeInMillis() - (1000*60*5);
    	   
    	   url += " after:" + timestamp/1000;
    	   
    	   HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    	   
    	   JSONObject obj = new JSONObject(response.getBody());
    	   try{
    		   int result = obj.getInt("resultSizeEstimate");
    		   if(result > 0){
    			   JSONArray messages = obj.getJSONArray("messages");
	    		   for(int i=0; i< messages.length(); i++){
	    			   JSONObject message = messages.getJSONObject(i);
	    			   String messageId = message.getString("id");
	    			   String messageUrl = "https://www.googleapis.com/gmail/v1/users/me/messages/" + messageId;
	    			   
	    			   HttpEntity<String> messageResponse = restTemplate.exchange(messageUrl, HttpMethod.GET, entity, String.class);
	    			   System.out.println(messageResponse.getBody());
	    		   }
	    		      		   
    			   List<ActionIngredient> actionIngredients = actionIngredientService.getRecipeActionIngredients(recipe.getId());
    			   if(recipe.getAction().getChannel().getKeyword().equals("gmail")){
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
	    	    	   
	    	    	   GmailUtil.sendEmail(to, subject, body, auth.getTokenType(), auth.getAccessToken());
    			   }
    		   }
    	   }catch (Exception e){
    		   e.printStackTrace();
    	   }
       }
       
    }
   */
	
	
}