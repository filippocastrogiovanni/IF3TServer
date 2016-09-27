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
import if3t.apis.WeatherUtil;
import if3t.apis.WeatherUtil.SunriseSunsetMode;
import if3t.apis.WeatherUtil.TempAboveBelowMode;
import if3t.apis.WeatherUtil.UnitsFormat;
import if3t.entities.ActionIngredient;
import if3t.entities.Authorization;
import if3t.entities.Channel;
import if3t.entities.ParametersActions;
import if3t.entities.Recipe;
import if3t.entities.TriggerIngredient;
import if3t.entities.User;
import if3t.exceptions.InvalidParametersException;
import if3t.services.ActionIngredientService;
import if3t.services.AuthorizationService;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;

@Component
public class WeatherTasks 
{
	@Autowired
	private WeatherUtil weatherUtil;
	@Autowired
	private GmailUtil gmailUtil;
	@Autowired
	private TwitterUtil twitterUtil;
	@Autowired
	private FacebookUtil facebookUtil;
	@Autowired
	private GoogleCalendarUtil gcalendarUtil;
	@Autowired
    private RecipeService recipeService;
	@Autowired
	private AuthorizationService authService;
	@Autowired
	private TriggerIngredientService triggerIngrService;
	@Autowired
	private ActionIngredientService actionIngrService;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
	
	@Scheduled(initialDelay = 2 * 30 * 1000, fixedRateString = "${app.scheduler.value.weather}")
	public void tomorrowWeatherScheduler()
	{		
		for (Recipe recipe : recipeService.getEnabledRecipesByTriggerChannel("weather"))
		{			
			User user = recipe.getUser();
			Channel triggerChannel = recipe.getTrigger().getChannel();
			Authorization authTrigger = authService.getAuthorization(user.getId(), triggerChannel.getKeyword());
			
			if (authTrigger == null)
			{
				logger.info("There is no location associated with the weather channel for the user " + user.getUsername());
				continue;
			}
			
			Channel actionChannel = recipe.getAction().getChannel();
			Authorization authAction = authService.getAuthorization(user.getId(), actionChannel.getKeyword());
			
			if (authAction == null)
			{
				logger.info("Action channel (" + actionChannel.getKeyword() + ") is not enabled for the user " + user.getUsername());
				continue;
			}
			
			String report = null;
			List<TriggerIngredient> trigIngrList = triggerIngrService.getRecipeTriggerIngredients(recipe.getId());
			
			if (recipe.getTrigger().getId() == 4) 
			{
				String time = "";
				
				for (TriggerIngredient ti : trigIngrList)
				{				
					if (ti.getParam().getKeyword().equals("time")) {
						time = ti.getValue();
					}
				}
				
				// WARNING: the access_token field is used to store the id of the location associated with the weather channel
				report = weatherUtil.getTomorrowReport(Long.parseLong(authTrigger.getAccessToken()), recipe.getId(), time, user.getTimezone().getZone_id(), UnitsFormat.CELSIUS);
			}
			else if (recipe.getTrigger().getId() == 5) 
			{
				String below = "", above = "", temperature = "";
				
				for (TriggerIngredient ti : trigIngrList)
				{				
					if (ti.getParam().getKeyword().equals("below")) {
						below = ti.getValue();
					}
					else if (ti.getParam().getKeyword().equals("above")) {
						above = ti.getValue();
					}
					else if (ti.getParam().getKeyword().equals("temperature")) {
						temperature = ti.getValue();
					}
				}
				
				TempAboveBelowMode mode = (below.equals("below") && !above.equals("above")) ? TempAboveBelowMode.BELOW : TempAboveBelowMode.ABOVE;
				// WARNING: the access_token field is used to store the id of the location associated with the weather channel
				report = weatherUtil.getEventTemperatureAboveOrBelow(Long.parseLong(authTrigger.getAccessToken()), recipe.getId(), mode, Double.parseDouble(temperature), UnitsFormat.CELSIUS);
				
			}
			else if (recipe.getTrigger().getId() == 6) 
			{
				String sunrise = "", sunset = "";
				
				for (TriggerIngredient ti : trigIngrList)
				{				
					if (ti.getParam().getKeyword().equals("sunrise")) {
						sunrise = ti.getValue();
					}
					else if (ti.getParam().getKeyword().equals("sunset")) {
						sunset = ti.getValue();
					}
				}
				
				SunriseSunsetMode mode = (sunrise.equals("sunrise") && !sunset.equals("sunset")) ? SunriseSunsetMode.SUNRISE : SunriseSunsetMode.SUNSET;
				// WARNING: the access_token field is used to store the id of the location associated with the weather channel
				report = weatherUtil.getReportOnSunriseOrSunset(Long.parseLong(authTrigger.getAccessToken()), recipe.getId(), mode, UnitsFormat.CELSIUS);
			}
			else 
			{
				logger.error("This trigger is not associated with the weather channel!");
				continue;
			}
			
			
			if (report == null) {
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
					String post = "";
					
					for (ActionIngredient ai: actionIngredients)
					{
						ParametersActions param = ai.getParam();

						if (param.getKeyword().equals("post")) {
							post = param.getCanReceive() ? weatherUtil.replaceKeywords(ai.getValue(), trigIngrList, param.getMaxLength()) : ai.getValue();
						}
					}
					
					try
					{
						post += "\n-------------------------------\n" + report;
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
					
					break;
				}
				case "gcalendar":
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
								title = param.getCanReceive() ? weatherUtil.replaceKeywords(ai.getValue(), trigIngrList, param.getMaxLength()) : ai.getValue();
								break;
							case "description" :
								description = param.getCanReceive() ? weatherUtil.replaceKeywords(ai.getValue(), trigIngrList, param.getMaxLength()) : ai.getValue();
								break;
							case "location" :
								location = param.getCanReceive() ? weatherUtil.replaceKeywords(ai.getValue(), trigIngrList, param.getMaxLength()) : ai.getValue();
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
	
						description += "\n-------------------------------\n" + report;
						gcalendarUtil.createEvent(start, end, title, description, location, authAction);
						logger.info("Event created on the calendar of the user " + user.getUsername());
						
					}
					catch (Throwable t)
					{
						logger.error(t.getClass().getCanonicalName(), t);
					}
					
					break;
				}
				case "gmail":
				{
					String to = "", subject = "", body = "";
					
					for (ActionIngredient ai : actionIngredients)
					{
						ParametersActions param = ai.getParam();
						
						if (param.getKeyword().equals("to_address")) {
							to = param.getCanReceive() ? weatherUtil.replaceKeywords(ai.getValue(), trigIngrList, param.getMaxLength()) : ai.getValue();
						}
						else if (param.getKeyword().equals("subject")) {
							subject = param.getCanReceive() ? weatherUtil.replaceKeywords(ai.getValue(), trigIngrList, param.getMaxLength()) : ai.getValue();
						}
						else if (param.getKeyword().equals("body")) {
							body = param.getCanReceive() ? weatherUtil.replaceKeywords(ai.getValue(), trigIngrList, param.getMaxLength()) : ai.getValue();
						}
					}
					
					try
					{
						body += "\n-------------------------------\n" + report;
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
					
					break;
				}
				case "twitter":
				{
					String tweet = "", hashtag = "";
					
					for (ActionIngredient ai : actionIngredients)
					{
						ParametersActions param = ai.getParam();
						
						if (param.getKeyword().equals("tweet")) {
							tweet = param.getCanReceive() ? weatherUtil.replaceKeywords(ai.getValue(), trigIngrList, param.getMaxLength()) : ai.getValue();
						}
						else if (param.getKeyword().equals("hashtag")) {
							hashtag = param.getCanReceive() ? weatherUtil.replaceKeywords(ai.getValue(), trigIngrList, param.getMaxLength()) : ai.getValue();
						}
					}
					
					tweet += "\n-------------------------------\n" + report;
					twitterUtil.postTweet(user.getId(), authAction, tweet, hashtag);
					
					break;
				}
			}
		}
	}
}
