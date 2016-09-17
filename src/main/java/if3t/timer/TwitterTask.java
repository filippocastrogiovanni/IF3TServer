package if3t.timer;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import if3t.apis.GmailUtil;
import if3t.apis.TwitterUtil;
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
import twitter4j.Status;

@Component
public class TwitterTask 
{
	@Autowired
	private TwitterUtil twitterUtil;
	@Autowired
	private GmailUtil gmailUtil;
	@Autowired
    private RecipeService recipeService;
	@Autowired
	private TriggerIngredientService triggerIngrService;
	@Autowired
	private ActionIngredientService actionIngrService;
	@Autowired
	private AuthorizationService authService;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
	
	
	//TODO diversificare i fixedRate dei vari task per non creare dei picchi di lavoro estremi intervallati dal nulla
	@Scheduled(fixedRate = 1000*60*5)
    public void twitterScheduler() 
	{
    	//TODO forse va modificato il nome della funzione per evidenziare che ritorna solo le ricette enabled
		List<Recipe> twitterTriggerRecipes = recipeService.getRecipeByTriggerChannel("twitter");
		
		for (Recipe recipe : twitterTriggerRecipes)
		{
			User user = recipe.getUser();
			Channel triggerChannel = recipe.getTrigger().getChannel();
			Authorization authTrigger = authService.getAuthorization(user.getId(), triggerChannel.getKeyword());
			
			if (authTrigger == null)
			{
				logger.info("Trigger channel (" + triggerChannel.getKeyword() + ") is not enabled for the user " + user.getUsername());
				continue;
			}
			
			Channel actionChannel = recipe.getAction().getChannel();
			Authorization authAction = authService.getAuthorization(user.getId(), actionChannel.getKeyword());
			
			if (authAction == null)
			{
				logger.info("Action channel (" + actionChannel.getKeyword() + ") is not enabled for the user " + user.getUsername());
				continue;
			}
			
			//TODO aggiornare il db con le keyword dei parametri e caricarlo sul drive
			//TODO estendere considerando i possibili valori dei campi del trigger (tabella Alessio)
			//TODO creare la tabella per i logs e riempirla qui. Serve inoltre un task separato che cancella le tuple antecedenti una certa data
			switch (recipe.getAction().getChannel().getKeyword())
			{
				case "gmail":
				{
					if (authAction.getExpireDate() * 1000 <= Calendar.getInstance().getTimeInMillis())
					{
						logger.info("Action channel (" + actionChannel.getKeyword() + "): token expired for the user " + user.getUsername());
						break;
					}
					
					String to = null, subject = "", body = "", hashtag = null, fromUser = null;
					List<TriggerIngredient> triIngredients = triggerIngrService.getRecipeTriggerIngredients(recipe.getId());
					List<ActionIngredient> actIngredients = actionIngrService.getRecipeActionIngredients(recipe.getId());
					
					for (TriggerIngredient ti : triIngredients)
					{
						ParametersTriggers param = ti.getParam();
						
						if (param.getKeyword().equals("hashtag")) {
							hashtag = ti.getValue();
						}
						else if (param.getKeyword().equals("user")) {
							fromUser = ti.getValue();
						}
					}
					
					List<Status> newUsefulTweets = twitterUtil.getNewUsefulTweets(user.getId(), recipe.getId(), authTrigger, hashtag, fromUser);
					
					if (newUsefulTweets.isEmpty()) {
						break;
					}
					
					for (ActionIngredient ai : actIngredients)
					{
						ParametersActions param = ai.getParam();
						
						if (param.getKeyword().equals("to")) {
							to = ai.getValue();
						}
						else if (param.getKeyword().equals("subject")) {
							subject = ai.getValue();
						}
						else if (param.getKeyword().equals("body")) {
							body = ai.getValue();
						}
					}
					
					for (Status tweet : newUsefulTweets)
					{
						//FIXME rimuovere alla fine
						//System.out.println(tweet.getText());
						//TODO da fare solo quando specificato in fase di creazione della ricetta
						StringBuffer sb = new StringBuffer(body);
						sb.append("\n---- Content of the tweet ----\n");
						sb.append(tweet.getText());
						
						try
						{
							gmailUtil.sendEmail(to, subject, sb.toString(), authAction);
							logger.info("Email sent from Gmail account of " + user.getUsername() + " to " + to);
						}
						catch (Throwable t)
						{
							logger.error(t.getClass().getCanonicalName(), t);
						}
					}
					
					break;
				}
			}
		}
	}
}
