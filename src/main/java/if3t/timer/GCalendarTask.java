package if3t.timer;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import if3t.apis.GoogleCalendarUtil;
import if3t.models.Authorization;
import if3t.models.Channel;
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
public class GCalendarTask {

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
	public void gCalendarScheduler(){
		
		/*Authorization auth = authService.getAuthorization(7l, "gcalendar");
		if(auth == null)
			return;
		try {
			GoogleCalendarUtil.isEventAdded(auth, 13131l);	
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
		
		
		List<Recipe> gCalendarTriggerRecipes = recipeService.getRecipeByTriggerChannel("gcalendar");
		for(Recipe recipe: gCalendarTriggerRecipes){
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
			for(TriggerIngredient triggerIngredient: triggerIngredients){
				ParametersTriggers param = triggerIngredient.getParam();
				switch(param.getKeyword()){
					case "add" :
						try {
							GoogleCalendarUtil.isEventAdded(triggerAuth, channelStatusService, user);
						} catch (IOException e) {
							e.printStackTrace();
							continue;
						}
						break;
					case "start" :
						break;
				}
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
