package if3t.timer;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
import if3t.entities.ParametersTriggers;
import if3t.entities.Recipe;
import if3t.entities.TriggerIngredient;
import if3t.entities.User;
import if3t.exceptions.InvalidParametersException;
import if3t.services.ActionIngredientService;
import if3t.services.AuthorizationService;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;
import twitter4j.Status;

@Component
public class TwitterTask 
{
	@Autowired
	private TwitterUtil twitterUtil;
	@Autowired
	private GmailUtil gmailUtil;
	@Autowired
	private FacebookUtil facebookUtil;
	@Autowired
	private GoogleCalendarUtil gcalendarUtil;
	@Autowired
    private RecipeService recipeService;
	@Autowired
	private TriggerIngredientService triggerIngrService;
	@Autowired
	private ActionIngredientService actionIngrService;
	@Autowired
	private AuthorizationService authService;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
	
	@Scheduled(initialDelay = 1 * 30 * 1000, fixedRateString = "${app.scheduler.value}")
    public void twitterScheduler() 
	{		
		for (Recipe recipe : recipeService.getEnabledRecipesByTriggerChannel("twitter"))
		{
			User user = recipe.getUser();
			Channel triggerChannel = recipe.getTrigger().getChannel();
			Authorization authTrigger = authService.getAuthorization(user.getId(), triggerChannel.getKeyword());
			
			if (authTrigger == null)
			{
				logger.info("Trigger channel (" + triggerChannel.getKeyword() + ") is not enabled for the user " + user.getUsername());
				continue;
			}
			
			Authorization authAction;
			Channel actionChannel = recipe.getAction().getChannel();
			
			if (actionChannel.getKeyword().equals(triggerChannel.getKeyword())) {
				authAction = authTrigger;
			}
			else 
			{
				authAction = authService.getAuthorization(user.getId(), actionChannel.getKeyword());
				
				if (authAction == null)
				{
					logger.info("Action channel (" + actionChannel.getKeyword() + ") is not enabled for the user " + user.getUsername());
					continue;
				}
			}
			
			long triggerId = recipe.getTrigger().getId();
			String hashtagTrigger = null, fromUser = null;
			List<TriggerIngredient> trigIngrList = triggerIngrService.getRecipeTriggerIngredients(recipe.getId());
			
			for (TriggerIngredient ti : trigIngrList)
			{
				ParametersTriggers param = ti.getParam();
				
				if (param.getKeyword().equals("hashtag")) {
					hashtagTrigger = ti.getValue();
				}
				else if (param.getKeyword().equals("user")) {
					fromUser = ti.getValue();
				}
			}
			
			List<Status> newUsefulTweets = twitterUtil.getNewUsefulTweets(user.getId(), recipe.getId(), authTrigger, hashtagTrigger, fromUser);
			
			if (newUsefulTweets.isEmpty()) {
				continue;
			}
			
			if (authAction.getExpireDate() != null && authAction.getExpireDate() <= Instant.now().getEpochSecond())
			{
				logger.info("Action channel (" + actionChannel.getKeyword() + "): token expired for the user " + user.getUsername());
				continue;
			}
			
			List<ActionIngredient> actionIngredients = actionIngrService.getRecipeActionIngredients(recipe.getId());
			
			switch (recipe.getAction().getChannel().getKeyword())
			{
				case "facebook":
				{
					for (Status tweet : newUsefulTweets)
					{	
						String post = "";
						
						for (ActionIngredient ai: actionIngredients)
						{
							ParametersActions param = ai.getParam();
		
							if (param.getKeyword().equals("post")) {
								post = param.getCanReceive() ? twitterUtil.replaceKeywords(ai.getValue(), triggerId, trigIngrList, tweet, param.getMaxLength()) : ai.getValue();
							}
						}
						
						try
						{
							facebookUtil.publish_new_post(post, authAction.getAccessToken());
							logger.info("A new post has been submitted on the Facebook page of the user " + user.getUsername());
						}
						catch (FacebookOAuthException e)
						{
							logger.error("Too many post in a short time on the Facebook page of the user " + user.getUsername(), e);
						}
						catch (Throwable t)
						{
							logger.error(t.getClass().getCanonicalName(), t);
						}
					}
					
					break;
				}
				case "gcalendar":
				{
					for (Status tweet : newUsefulTweets)
					{	
						String title = "", location = "", description = "";
						String startDateString = "", endDateString = "", startTimeString = "", endTimeString = "";
						
						for (ActionIngredient ai: actionIngredients)
						{
							ParametersActions param = ai.getParam();
	
							switch (param.getKeyword())
							{
								case "start_date" :
									startDateString = ai.getValue();
									break;
								case "end_date" :
									endDateString = ai.getValue();
									break;
								case "start_time" :
									startTimeString = ai.getValue();
									break;
								case "end_time" :
									endTimeString = ai.getValue();
									break;
								case "title" :
									title = param.getCanReceive() ? twitterUtil.replaceKeywords(ai.getValue(), triggerId, trigIngrList, tweet, param.getMaxLength()) : ai.getValue();
									break;
								case "description" :
									description = param.getCanReceive() ? twitterUtil.replaceKeywords(ai.getValue(), triggerId, trigIngrList, tweet, param.getMaxLength()) : ai.getValue();
									break;
								case "location" :
									location = param.getCanReceive() ? twitterUtil.replaceKeywords(ai.getValue(), triggerId, trigIngrList, tweet, param.getMaxLength()) : ai.getValue();
									break;
							}
						}
						
						try
						{
							TimeZone timezone = TimeZone.getTimeZone(user.getTimezone().getZone_id());
							SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		
							String startDate = startDateString + " " + startTimeString;
							String endDate = endDateString + " " + endTimeString;
							Calendar start = Calendar.getInstance();
							Calendar end = Calendar.getInstance();
							start.setTime(format.parse(startDate));
							start.setTimeZone(timezone);
							end.setTime(format.parse(endDate));
							end.setTimeZone(timezone);
		
							gcalendarUtil.createEvent(start, end, title, description, location, authAction);
							logger.info("Event created on the calendar of the user " + user.getUsername());
							
						}
						catch (Throwable t)
						{
							logger.error(t.getClass().getCanonicalName(), t);
						}
					}
					
					break;
				}
	
				case "gmail":
				{	
					for (Status tweet : newUsefulTweets)
					{	
						String to = "", subject = "", body = "";
						
						for (ActionIngredient ai : actionIngredients)
						{
							ParametersActions param = ai.getParam();
							
							if (param.getKeyword().equals("to_address")) {
								to = param.getCanReceive() ? twitterUtil.replaceKeywords(ai.getValue(), triggerId, trigIngrList, tweet, param.getMaxLength()) : ai.getValue();
							}
							else if (param.getKeyword().equals("subject")) {
								subject = param.getCanReceive() ? twitterUtil.replaceKeywords(ai.getValue(), triggerId, trigIngrList, tweet, param.getMaxLength()) : ai.getValue();
							}
							else if (param.getKeyword().equals("body")) {
								body = param.getCanReceive() ? twitterUtil.replaceKeywords(ai.getValue(), triggerId, trigIngrList, tweet, param.getMaxLength()) : ai.getValue();
							}
						}
							
						try
						{
							gmailUtil.sendEmail(to, subject, body, authAction);
							logger.info("Email sent from Gmail account of " + user.getUsername() + " to " + to);
						}
						catch (InvalidParametersException e)
						{
							logger.error("Recipe " + recipe.getId() + ": " + e.getMessage());
						}
						catch (Throwable t)
						{
							logger.error(t.getClass().getCanonicalName(), t);
						}
					
					}
					
					break;
				}
				case "twitter":
				{
					for (Status tweet : newUsefulTweets)
					{
						String tweetAction = "", hashtagAction = "";
						
						for (ActionIngredient ai : actionIngredients)
						{
							ParametersActions param = ai.getParam();
							
							if (param.getKeyword().equals("tweet")) {
								tweetAction = param.getCanReceive() ? twitterUtil.replaceKeywords(ai.getValue(), triggerId, trigIngrList, tweet, param.getMaxLength()) : ai.getValue();
							}
							else if (param.getKeyword().equals("hashtag")) {
								hashtagAction = param.getCanReceive() ? twitterUtil.replaceKeywords(ai.getValue(), triggerId, trigIngrList, tweet, param.getMaxLength()) : ai.getValue();
							}
						}
					
						twitterUtil.postTweet(user.getId(), authAction, tweetAction, hashtagAction);
					}
					
					break;
				}
			}
		}
	}
}
