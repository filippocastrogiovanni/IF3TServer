package if3t.timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.api.services.gmail.model.Message;
import com.restfb.exception.FacebookOAuthException;

import if3t.apis.FacebookUtil;
import if3t.apis.GmailUtil;
import if3t.apis.GoogleCalendarUtil;
import if3t.apis.TwitterUtil;
import if3t.entities.ActionIngredient;
import if3t.entities.Authorization;
import if3t.entities.Channel;
import if3t.entities.ChannelStatus;
import if3t.entities.ParametersActions;
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
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

	@Scheduled(initialDelay = 3 * 30 * 1000, fixedRateString = "${app.scheduler.value}")
	public void gmailScheduler(){

		List<Recipe> gmailTriggerRecipes = recipeService.getEnabledRecipesByTriggerChannel("gmail");
		for(Recipe recipe: gmailTriggerRecipes){
			try{
				User user = recipe.getUser();
				TimeZone timezone = TimeZone.getTimeZone(user.getTimezone().getZone_id());
				Channel triggerChannel = recipe.getTrigger().getChannel();
				Channel actionChannel = recipe.getAction().getChannel();
				Authorization triggerAuth = authService.getAuthorization(user.getId(), triggerChannel.getKeyword());
				Authorization actionAuth = authService.getAuthorization(user.getId(), actionChannel.getKeyword());

				//Checking if the access token of the trigger channel is expired
				Calendar now = Calendar.getInstance();
				if(triggerAuth == null || triggerAuth.getExpireDate()*1000 <= now.getTimeInMillis()){
					logger.info("Gmail channel not authorized or expired for the user " + user.getUsername());
					continue;
				}

				//Checking if the access token of the action channel is not present
				if(actionAuth == null){
					logger.info("Action channel (" + actionChannel.getKeyword() + ") is not enabled for the user " + user.getUsername());
					continue;
				}

				List<TriggerIngredient> triggerIngredients = triggerIngredientService.getRecipeTriggerIngredients(recipe.getId());

				Long timestamp = 0l;

				ChannelStatus channelStatus = channelStatusService.readChannelStatusByRecipeId(recipe.getId());
				if(channelStatus == null){
					timestamp = Calendar.getInstance().getTimeInMillis()- (rate);
					channelStatus = channelStatusService.createNewChannelStatus(recipe.getId(), timestamp);
				}
				else
					timestamp = channelStatus.getSinceRef();
	
				List<Message> messages = gmailUtil.checkEmailReceived(triggerAuth, triggerIngredients, recipe);

				if(messages.size() > 0){
					messages = gmailUtil.getMessages(triggerAuth, messages);
					List<ActionIngredient> actionIngredients = actionIngredientService.getRecipeActionIngredients(recipe.getId());

					//Checking if the access token of the action channel is expired
					now = Calendar.getInstance();
					if(actionAuth.getExpireDate() == null || actionAuth.getExpireDate()*1000 <= now.getTimeInMillis()){
						logger.info("Action channel (" + actionChannel.getKeyword() + "): token expired for the user " + user.getUsername());
						continue;
					}
					switch(recipe.getAction().getChannel().getKeyword()){
						case "gmail" :
							for(Message message : messages){
								System.out.println(message.toPrettyString());
								String to = "";
								String subject = "";
								String body = "";
	
								for(ActionIngredient actionIngredient: actionIngredients){
									ParametersActions actionParam = actionIngredient.getParam();
	
									switch(actionParam.getKeyword()){
										case "to_address" :
											to = actionIngredient.getValue();
											break;
										case "subject" :
											if(actionParam.getCanReceive())
												subject = gmailUtil.validateAndReplaceKeywords(actionIngredient.getValue(), actionParam.getMaxLength(), message);
											else
												subject = actionIngredient.getValue();
											break;
										case "body" :
											if(actionParam.getCanReceive())
												body = gmailUtil.validateAndReplaceKeywords(actionIngredient.getValue(), actionParam.getMaxLength(), message);
											else
												body = actionIngredient.getValue();
											break;	
									}
								}
								gmailUtil.sendEmail(to, subject, body, actionAuth);
								logger.info("Email sent from Gmail account of " + user.getUsername() + " to " + to);
							}
							break;
						case "gcalendar" :
							for(Message message : messages){
								String title = "";
								String location = "";
								String description = "";
								String startDateString = "";
								String endDateString = "";
								String startTimeString = "";
								String endTimeString = "";
								
								for(ActionIngredient actionIngredient: actionIngredients){
									ParametersActions actionParam = actionIngredient.getParam();
			
									switch(actionParam.getKeyword()){
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
											if(actionParam.getCanReceive())
												title = gmailUtil.validateAndReplaceKeywords(actionIngredient.getValue(), actionParam.getMaxLength(), message);
											else
												title = actionIngredient.getValue();
											break;
										case "description" :
											if(actionParam.getCanReceive())
												description = gmailUtil.validateAndReplaceKeywords(actionIngredient.getValue(), actionParam.getMaxLength(), message);
											else
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
		
								gCalendarUtil.createEvent(start, end, title, description, location, actionAuth);
								logger.info("Event created on the calendar of the user " + user.getUsername());
							}
							break;
						case "facebook" :
							for(Message message : messages){
								String post = "";
								for(ActionIngredient actionIngredient: actionIngredients){
									ParametersActions actionParam = actionIngredient.getParam();
		
									if(actionParam.getKeyword().equals("post")){
										if(actionParam.getCanReceive())
											post = gmailUtil.validateAndReplaceKeywords(actionIngredient.getValue(), actionParam.getMaxLength(), message);
										else
											post = actionIngredient.getValue();
									}
								}
								try{
									facebookUtil.publish_new_post(post, actionAuth.getAccessToken());
									logger.info("A new post has been submitted on the Facebook page of the user " + user.getUsername());
								}
								catch(FacebookOAuthException e){
									logger.error("Too many post in a short time on the Facebook page of the user " + user.getUsername(), e);
								}
							}
							break;
						case "twitter" :
							for(Message message : messages){
								String tweet = "";
								String hashtag = "";
								for(ActionIngredient actionIngredient: actionIngredients){
									ParametersActions actionParam = actionIngredient.getParam();
		
									switch(actionParam.getKeyword()){
										case "tweet" :
											if(actionParam.getCanReceive())
												tweet = gmailUtil.validateAndReplaceKeywords(actionIngredient.getValue(), actionParam.getMaxLength(), message);
											else
												tweet = actionIngredient.getValue();
											break;
										case "hashtag" :
											if(actionParam.getCanReceive())
												hashtag = gmailUtil.validateAndReplaceKeywords(actionIngredient.getValue(), actionParam.getMaxLength(), message);
											else
												hashtag = actionIngredient.getValue();
											break;
									}
								}
								twitterUtil.postTweet(user.getId(), actionAuth, tweet, hashtag);
							}
							break;
					}
				}
			}catch (Exception e){
				logger.error(e.getMessage(), e);
				continue;
			}
		}

	}
	
	
}