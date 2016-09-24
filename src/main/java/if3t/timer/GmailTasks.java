package if3t.timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
import if3t.apis.TwitterUtil;
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
	private TwitterUtil twitterUtil;
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
	@Value("${app.timezone}")
	private String zone;

	@Scheduled(fixedRateString = "${app.scheduler.value}")
	public void gmailScheduler(){
		TimeZone timezone = TimeZone.getTimeZone(zone);
		//RestTemplate restTemplate = new RestTemplate();

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

				/*HttpHeaders headers = new HttpHeaders();
				headers.set("Authorization", triggerAuth.getTokenType() + " " + triggerAuth.getAccessToken());
				HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

				String url = "https://www.googleapis.com/gmail/v1/users/"
						+ "me/messages?"
						+ "q=";
*/
				
				List<TriggerIngredient> triggerIngredients = triggerIngredientService.getRecipeTriggerIngredients(recipe.getId());
				
				/*for(TriggerIngredient triggerIngredient: triggerIngredients){
					ParametersTriggers param = triggerIngredient.getParam();
					if(param.getKeyword().equals("from_address"))
						url += "from:" + triggerIngredient.getValue();
				}*/

				Long timestamp = 0l;

				ChannelStatus channelStatus = channelStatusService.readChannelStatusByRecipeId(recipe.getId());
				if(channelStatus == null)
					timestamp = Calendar.getInstance().getTimeInMillis()- (rate);
				else
					timestamp = channelStatus.getSinceRef();

				//url += " after:" + timestamp/1000;

				//HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

				List<Message> messages = gmailUtil.checkEmailReceived(triggerAuth, triggerIngredients, timestamp, recipe);

				channelStatusService.updateChannelStatus(user.getId(), timestamp + (1000*60*5));

				if(messages.size() > 0){
					List<ActionIngredient> actionIngredients = actionIngredientService.getRecipeActionIngredients(recipe.getId());

					//Checking if the access token of the action channel is expired
					now = Calendar.getInstance();
					if(actionAuth.getExpireDate()*1000 <= now.getTimeInMillis())
						continue;

					switch(recipe.getAction().getChannel().getKeyword()){
					case "gmail" :
						for(Message message : messages){
							String to = "";
							String subject = "";
							String body = "";

							for(ActionIngredient actionIngredient: actionIngredients){
								ParametersActions param = actionIngredient.getParam();

								if(param.getKeyword().equals("to_address"))
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
						String title = "";
						String location = "";
						String description = "";
						String startDateString = "";
						String endDateString = "";
						String startTimeString = "";
						String endTimeString = "";
						
						for(ActionIngredient actionIngredient: actionIngredients){
							ParametersActions param = actionIngredient.getParam();
	
							switch(param.getKeyword()){
								case "start_date" :
									startDateString = actionIngredient.getValue();
									break;
								case "end_date" :
									endDateString = actionIngredient.getValue();
									break;
								case "start_time" :
									startTimeString = actionIngredient.getValue();
									break;
								case "end_time" :
									endTimeString = actionIngredient.getValue();
									break;
								case "title" :
									title = actionIngredient.getValue();
									break;
								case "description" :
									description = actionIngredient.getValue();
									break;
								case "location" :
									location = actionIngredient.getValue();
									break;
							}
						}
						
						SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");

						String startDate = startDateString + " " + startTimeString;
						String endDate = endDateString + " " + endTimeString;
						Calendar start = Calendar.getInstance();
						Calendar end = Calendar.getInstance();
						start.setTime(format.parse(startDate));
						start.setTimeZone(timezone);
						end.setTime(format.parse(endDate));
						end.setTimeZone(timezone);

						gCalendarUtil.createEvent(start, end, title, description, location, triggerAuth);
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
						String tweet = "";
						String hashtag = "";
						for(ActionIngredient actionIngredient: actionIngredients){
							ParametersActions param = actionIngredient.getParam();

							if(param.getKeyword().equals("tweet"))
								tweet += actionIngredient.getValue();
							if(param.getKeyword().equals("hashtag"))
								hashtag += actionIngredient.getValue();
						}
						twitterUtil.postTweet(user.getId(), actionAuth, tweet, hashtag);
						break;
					}
				}
				/*JSONObject obj = new JSONObject(response.getBody());

				int result = obj.getInt("resultSizeEstimate");
				if(result > 0){  		      		   
					
				}*/
			}catch (Exception e){
				e.printStackTrace();
				continue;
			}
		}

	}
	
	
}