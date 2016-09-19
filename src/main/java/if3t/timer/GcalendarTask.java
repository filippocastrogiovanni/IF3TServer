package if3t.timer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.api.services.calendar.model.Event;

import if3t.apis.FacebookUtil;
import if3t.apis.GmailUtil;
import if3t.apis.GoogleCalendarUtil;
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
import if3t.services.ChannelStatusService;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;

@Component
public class GcalendarTask {

	@Autowired
	private GmailUtil gmailUtil;
	@Autowired
	private GoogleCalendarUtil gCalendarUtil;
	@Autowired
	private FacebookUtil facebookUtil;
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
	@Value("${app.scheduler.value}")
	private long rate;
	
	@Scheduled(fixedRateString = "${app.scheduler.value}")
	public void gCalendarScheduler(){
		
		/*Authorization auth = authService.getAuthorization(7l, "gcalendar");
		if(auth == null)
			return;
		try {
			GoogleCalendarUtil.isEventAdded(auth, 13131l);	
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
		
		
		List<Recipe> gCalendarTriggerRecipes = recipeService.getEnabledRecipesByTriggerChannel("gcalendar");
		for(Recipe recipe: gCalendarTriggerRecipes){
			try{
				User user = recipe.getUser();
				Channel triggerChannel = recipe.getTrigger().getChannel();
				Channel actionChannel = recipe.getAction().getChannel();
				Authorization triggerAuth = authService.getAuthorization(user.getId(), triggerChannel.getKeyword());
				Authorization actionAuth = authService.getAuthorization(user.getId(), actionChannel.getKeyword());
				
				//Checking if the access token of the trigger channel is expired
				Calendar now = Calendar.getInstance();
				if(triggerAuth == null || triggerAuth.getExpireDate()*1000 <= now.getTimeInMillis()){
					//System.out.println("scaduto");
					continue;
				}
				
				//Checking if the access token of the action channel is not present
				if(actionAuth == null)
					continue;
				
				List<TriggerIngredient> triggerIngredients = triggerIngredientService.getRecipeTriggerIngredients(recipe.getId());
				TriggerIngredient triggerIngredient = triggerIngredients.get(0);
				ParametersTriggers triggerParam = triggerIngredient.getParam();
				
				List<Event> events = new ArrayList<>();
				switch(triggerParam.getKeyword()){
					case "add" :
						events = gCalendarUtil.checkEventsAdded(triggerAuth, recipe, triggerIngredient.getValue());
						break;
					case "start" :
						break;
				}
				
				if(!events.isEmpty()){
					List<ActionIngredient> actionIngredients = actionIngredientService.getRecipeActionIngredients(recipe.getId());
					
					//Checking if the access token of the action channel is expired
					now = Calendar.getInstance();
					if(actionAuth.getExpireDate()*1000 <= now.getTimeInMillis())
						continue;
					
					switch(recipe.getAction().getChannel().getKeyword()){
						case "gmail" :
							String to = "";
							String subject = "";
							String body = "";
							
							for(ActionIngredient actionIngredient: actionIngredients){
								ParametersActions actionParam = actionIngredient.getParam();
	
								if(actionParam.getKeyword().equals("to"))
									to = actionIngredient.getValue();
								if(actionParam.getKeyword().equals("subject"))
									subject = actionIngredient.getValue();
								if(actionParam.getKeyword().equals("body"))
									body = actionIngredient.getValue();
							}
							gmailUtil.sendEmail(to, subject, body, actionAuth);
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
				
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
		}
	}
		
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
}
