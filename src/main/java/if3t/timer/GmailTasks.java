package if3t.timer;

import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.api.services.gmail.model.Message;

import if3t.apis.FacebookUtil;
import if3t.apis.GmailUtil;
import if3t.apis.GoogleCalendarUtil;
import if3t.entities.ActionIngredient;
import if3t.entities.Authorization;
import if3t.entities.Channel;
import if3t.entities.ChannelStatus;
import if3t.entities.ParametersActions;
import if3t.entities.ParametersTriggers;
import if3t.entities.Recipe;
import if3t.entities.TriggerIngredient;
import if3t.entities.User;
import if3t.services.ActionIngredientService;
import if3t.services.AuthorizationService;
import if3t.services.ChannelStatusService;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;

@Component
public class GmailTasks {

	@Autowired
	private GmailUtil gmailUtil;
	@Autowired
	private GoogleCalendarUtil gCalendarUtil;
	@Autowired
    private RecipeService recipeService;
	@Autowired
	private FacebookUtil facebookUtil;
	@Autowired
	private TriggerIngredientService triggerIngredientService;
	@Autowired
	private ActionIngredientService actionIngredientService;
	@Autowired
	private AuthorizationService authService;
	@Autowired
	private ChannelStatusService channelStatusService;
	@Value("${app.scheduler.value}")
	private long rate;

	@Scheduled(fixedRateString = "${app.scheduler.value}")
	public void gmailScheduler(){
		RestTemplate restTemplate = new RestTemplate();

		List<Recipe> gmailTriggerRecipes = recipeService.getEnabledRecipesByTriggerChannel("gmail");
		for(Recipe recipe: gmailTriggerRecipes){
			try{
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

				ChannelStatus channelStatus = channelStatusService.readChannelStatusByRecipeId(recipe.getId());
				if(channelStatus == null)
					timestamp = Calendar.getInstance().getTimeInMillis()- (rate);
				else
					timestamp = channelStatus.getSinceRef();

				url += " after:" + timestamp/1000;

				HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

				List<Message> messages = gmailUtil.checkEmailReceived(triggerAuth, triggerIngredients, timestamp, recipe);

				channelStatusService.updateChannelStatus(user.getId(), timestamp + (1000*60*5));

				JSONObject obj = new JSONObject(response.getBody());

				int result = obj.getInt("resultSizeEstimate");
				if(result > 0){  		      		   
					List<ActionIngredient> actionIngredients = actionIngredientService.getRecipeActionIngredients(recipe.getId());

					//Checking if the access token of the action channel is expired
					now = Calendar.getInstance();
					if(actionAuth.getExpireDate()*1000 <= now.getTimeInMillis())
						continue;

					switch(recipe.getAction().getChannel().getKeyword()){
					case "gmail" :
						JSONArray messagesJSON = obj.getJSONArray("messages");
						for(int i=0; i< messagesJSON.length(); i++){
							JSONObject message = messagesJSON.getJSONObject(i);
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
						facebookUtil.publish_new_post(message, actionAuth.getAccessToken());
						break;
					case "twitter" :
						break;
					}
				}
			}catch (Exception e){
				e.printStackTrace();
				continue;
			}
		}

	}
	
	
}