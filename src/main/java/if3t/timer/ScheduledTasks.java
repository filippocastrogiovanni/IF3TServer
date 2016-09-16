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
	private GmailUtil gmailUtil;
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

	@Scheduled(fixedRate = 1000*60*5)
	public void gmailScheduler(){
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
								gmailUtil.sendEmail(to, subject, body, actionAuth);
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