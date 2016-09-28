package if3t.timer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.restfb.exception.FacebookOAuthException;

import if3t.apis.FacebookUtil;
import if3t.apis.GmailUtil;
import if3t.apis.GoogleCalendarUtil;
import if3t.apis.TwitterUtil;
import if3t.entities.ActionIngredient;
import if3t.entities.Authorization;
import if3t.entities.Channel;
import if3t.entities.ParametersActions;
import if3t.entities.Recipe;
import if3t.entities.TriggerIngredient;
import if3t.entities.User;
import if3t.services.ActionIngredientService;
import if3t.services.AuthorizationService;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;

@Component
public class FacebookTask {

	@Autowired
	private GmailUtil gmailUtil;
	@Autowired
	private GoogleCalendarUtil gCalendarUtil;
	@Autowired
	private FacebookUtil facebookUtil;
	@Autowired
	private TwitterUtil twitterUtil;
	@Autowired
    private RecipeService recipeService;
	@Autowired
	private TriggerIngredientService triggerIngredientService;
	@Autowired
	private ActionIngredientService actionIngredientService;
	@Autowired
	private AuthorizationService authService;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

	
	private ConcurrentHashMap<String, String> couples_access_token_full_names = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> couples_access_token_profile_pictures = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> couples_access_token_locations = new ConcurrentHashMap<String, String>();

	@Scheduled(initialDelay = 5 * 30 * 1000, fixedRateString = "${app.scheduler.value}")
	public void facebookScheduler(){
		List<Recipe> facebookTriggerRecipes = recipeService.getEnabledRecipesByTriggerChannel("facebook");
		for(Recipe recipe: facebookTriggerRecipes){
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
					logger.info("Facebook channel not authorized or expired for the user " + user.getUsername());
					continue;
				}

				//Checking if the access token of the action channel is not present
				if(actionAuth == null){
					logger.info("Action channel (" + actionChannel.getKeyword() + ") is not enabled for the user " + user.getUsername());
					continue;
				}
				
				long triggerId = recipe.getTrigger().getId();
				//take the trigger ingredients related to this facebook trigger
				//List<TriggerIngredient> related_facebook_ingredients = triggerIngredientService.getRecipeTriggerIngredients(recipe.getId());
				List<TriggerIngredient> related_facebook_ingredients = triggerIngredientService.getRecipeTriggerIngredients(recipe.getId());
				List<String> list_trigger_to_check = new ArrayList<>();
				for(TriggerIngredient parameter: related_facebook_ingredients){
					list_trigger_to_check.add(parameter.getValue());
				}
				
				//now I can have different situations/combinations:
				//-1 is the last case possible, that is "new_post"
				int situation=-1;
				if(!list_trigger_to_check.contains("full_name") && !list_trigger_to_check.contains("profile_picture") && !list_trigger_to_check.contains("location"))
					//new post
					situation = 0;
				else if(list_trigger_to_check.contains("full_name") && list_trigger_to_check.contains("profile_picture") && list_trigger_to_check.contains("location"))
					//every one
					situation = 1;
				else if(list_trigger_to_check.contains("full_name") && !list_trigger_to_check.contains("profile_picture") && !list_trigger_to_check.contains("location"))
					//full_name
					situation = 2;
				else if(!list_trigger_to_check.contains("full_name") && list_trigger_to_check.contains("profile_picture") && !list_trigger_to_check.contains("location"))
					//profile_picture
					situation = 3;
				else if(!list_trigger_to_check.contains("full_name") && !list_trigger_to_check.contains("profile_picture") && list_trigger_to_check.contains("location"))
					//location
					situation = 4;				
				else if(list_trigger_to_check.contains("full_name") && list_trigger_to_check.contains("profile_picture") && !list_trigger_to_check.contains("location"))
					//full_name, profile_picture
					situation = 5;					
				else if(list_trigger_to_check.contains("full_name") && !list_trigger_to_check.contains("profile_picture") && list_trigger_to_check.contains("location"))
					//full_name, location
					situation = 6;	
				else if(!list_trigger_to_check.contains("full_name") && list_trigger_to_check.contains("profile_picture") && list_trigger_to_check.contains("location"))
					//profile_picture, location
					situation = 7;	
				
				boolean full_name_changed = false, profile_picture_changed = false, location_changed = false;
				ArrayList<String> new_posts = new ArrayList<String>();
				String access_token = triggerAuth.getAccessToken();
				
				//the server should never stop, so I do not need to have a table in the DB with full_name, profile_picture, location history
				
				try{
					switch(situation){
						//the server can not wait for the verification of all the conditions because they could never happen, so we just take into account a moment, if in that moment the conditions are verified, then we proceed with the action
						case 0://new post
							new_posts = facebookUtil.calculate_new_posts_by_user_number(access_token, recipe.getId());
						   	if(new_posts.size() <= 0)
						   		continue; 	
							break;
						case 1://every one
							full_name_changed = facebookUtil.is_full_name_changed(access_token, couples_access_token_full_names, recipe.getId());
							profile_picture_changed = facebookUtil.is_profile_picture_changed(access_token, couples_access_token_profile_pictures, recipe.getId());
							location_changed = facebookUtil.is_location_changed(access_token, couples_access_token_locations, recipe.getId());;
						   	if(!(full_name_changed && profile_picture_changed && location_changed))
						   		continue;
							break;
						case 2://full_name
							full_name_changed = facebookUtil.is_full_name_changed(access_token, couples_access_token_full_names, recipe.getId());
						   	if(!full_name_changed)
						   		continue;
							break;
						case 3://profile_picture
							profile_picture_changed = facebookUtil.is_profile_picture_changed(access_token, couples_access_token_profile_pictures, recipe.getId());
						   	if(!profile_picture_changed)
						   		continue;
							break;
						case 4://location
							location_changed = facebookUtil.is_location_changed(access_token, couples_access_token_locations, recipe.getId());
						   	if(!(location_changed))
						   		continue;					
							break;
						case 5://full_name, profile_picture
							full_name_changed = facebookUtil.is_full_name_changed(access_token, couples_access_token_full_names, recipe.getId());
							profile_picture_changed = facebookUtil.is_profile_picture_changed(access_token, couples_access_token_profile_pictures, recipe.getId());
						   	if(!(full_name_changed && profile_picture_changed))
						   		continue;
							break;
						case 6://full_name, location
							full_name_changed = facebookUtil.is_full_name_changed(access_token, couples_access_token_full_names, recipe.getId());
							location_changed = facebookUtil.is_location_changed(access_token, couples_access_token_locations, recipe.getId());
						   	if(!(full_name_changed && location_changed))
						   		continue;
							break;		
						case 7://profile_picture, location
							profile_picture_changed = facebookUtil.is_profile_picture_changed(access_token, couples_access_token_profile_pictures, recipe.getId());
							location_changed = facebookUtil.is_location_changed(access_token, couples_access_token_locations, recipe.getId());
						   	if(!(profile_picture_changed && location_changed))
						   		continue;
							break;	
					}
				}catch(Exception e){
					//System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
					logger.error(e.getMessage(), e);
				}
				
				//here the trigger has been happened, so we can proceed with the action
				List<ActionIngredient> actionIngredients = actionIngredientService.getRecipeActionIngredients(recipe.getId());

				//Checking if the access token of the action channel is expired
				now = Calendar.getInstance();
				if(actionAuth.getExpireDate()*1000 <= now.getTimeInMillis())
					continue;
				
			
				//we need to repeat the action for each new post, or just once 
				if(situation != 0){ //not new_post as trigger
					switch(recipe.getAction().getChannel().getKeyword()){
					
					case "facebook" :
						String message = "";
						for(ActionIngredient actionIngredient: actionIngredients){
							ParametersActions param = actionIngredient.getParam();
	
							if(param.getKeyword().equals("post"))
								message = actionIngredient.getValue();
						}
						try{
							facebookUtil.publish_new_post(message, actionAuth.getAccessToken());
							logger.info("A new post has been submitted on the Facebook page of the user " + user.getUsername());
						}catch(FacebookOAuthException e){
							logger.error("Too many post in a short time on the Facebook page of the user " + user.getUsername(), e);
						}
						break;
					case "gmail" :
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
									subject = actionIngredient.getValue();
									break;
								case "body" :
									body = actionIngredient.getValue();
									break;
							}		
						}
						gmailUtil.sendEmail(to, subject, body, actionAuth);
						logger.info("Email sent from Gmail account of " + user.getUsername() + " to " + to);
						break;
					case "gcalendar" :
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

						gCalendarUtil.createEvent(start, end, title, description, location, actionAuth);
						logger.info("Event created on the calendar of the user " + user.getUsername());
						break;
					case "twitter" :
						String tweet = "";
						String hashtag = "";
						for(ActionIngredient actionIngredient: actionIngredients){
							ParametersActions actionParam = actionIngredient.getParam();

							switch(actionParam.getKeyword()){
								case "tweet" :
									tweet = actionIngredient.getValue();
									break;
								case "hashtag" :
									hashtag = actionIngredient.getValue();
									break;
							}
						}
						twitterUtil.postTweet(user.getId(), actionAuth, tweet, hashtag);
						break;
					}
				}
				else{
					switch(recipe.getAction().getChannel().getKeyword()){
						case "facebook" :
							for(String post : new_posts){
								String message = "";
								for(ActionIngredient actionIngredient: actionIngredients){
									ParametersActions actionParam = actionIngredient.getParam();
	
									if(actionParam.getKeyword().equals("post")){
										if(actionParam.getCanReceive())
											message = facebookUtil.validateAndReplaceKeywords(actionIngredient.getValue(), triggerId, actionParam.getMaxLength(), post);
										else
											message = actionIngredient.getValue();
									}
								}
								try{
									facebookUtil.publish_new_post(message, actionAuth.getAccessToken());
									logger.info("A new post has been submitted on the Facebook page of the user " + user.getUsername());

								}
								catch(FacebookOAuthException e){
									logger.error("Too many post in a short time on the Facebook page of the user " + user.getUsername(), e);
								}
							}
							break;
						case "gmail" :
							for(String post : new_posts){
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
												subject = facebookUtil.validateAndReplaceKeywords(actionIngredient.getValue(), triggerId, actionParam.getMaxLength(), post);
											else
												subject = actionIngredient.getValue();
											break;
										case "body" :
											body = actionIngredient.getValue();
											break;
									}		
								}
								gmailUtil.sendEmail(to, subject, body, actionAuth);
								logger.info("Email sent from Gmail account of " + user.getUsername() + " to " + to);
							}
							break;
						case "gcalendar" :
							for(String post : new_posts){
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
												title = facebookUtil.validateAndReplaceKeywords(actionIngredient.getValue(), triggerId, actionParam.getMaxLength(), post);
											else
												title = actionIngredient.getValue();
											break;
										case "description" :
											if(actionParam.getCanReceive())
												description = facebookUtil.validateAndReplaceKeywords(actionIngredient.getValue(), triggerId, actionParam.getMaxLength(), post);
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
						case "twitter" :
							for(String post : new_posts){
								String tweet = "";
								String hashtag = "";
								
								for(ActionIngredient actionIngredient: actionIngredients){
									ParametersActions actionParam = actionIngredient.getParam();
	
									switch(actionParam.getKeyword()){
										case "tweet" :
											if(actionParam.getCanReceive())
												tweet = facebookUtil.validateAndReplaceKeywords(actionIngredient.getValue(), triggerId, actionParam.getMaxLength(), post);
											else
												tweet = actionIngredient.getValue();
											break;
										case "hashtag" :
											if(actionParam.getCanReceive())
												hashtag = facebookUtil.validateAndReplaceKeywords(actionIngredient.getValue(), triggerId, actionParam.getMaxLength(), post);
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
	
	
	/*hashmaps with one row for each user who has this trigger
	private ConcurrentHashMap<String, String> couples_access_token_full_names = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> couples_access_token_profile_pictures = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> couples_access_token_locations = new ConcurrentHashMap<String, String>();

    
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

