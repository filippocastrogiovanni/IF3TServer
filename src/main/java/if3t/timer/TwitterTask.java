package if3t.timer;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import if3t.apis.GmailUtil;
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
    private RecipeService recipeService;
	@Autowired
	private TriggerIngredientService triggerIngrService;
	@Autowired
	private ActionIngredientService actionIngrService;
	@Autowired
	private AuthorizationService authService;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
	
	
	//TODO diversificare i fixedRate dei vari task per non creare dei picchi di lavoro estremi intervallati dal nulla
	//TODO quello da far partire per primo è quello relativo al refresh
	@Scheduled(fixedRateString = "${app.scheduler.value}")
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
			
			String hashtagTrigger = null, fromUser = null;
			
			for (TriggerIngredient ti : triggerIngrService.getRecipeTriggerIngredients(recipe.getId()))
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
			
			//TODO aggiornare il db con le keyword dei parametri e caricarlo sul drive
			//TODO estendere considerando i possibili valori dei campi del trigger (tabella Alessio)
			//TODO creare la tabella per i logs e riempirla qui. Serve inoltre un task separato che cancella le tuple antecedenti una certa data
			switch (recipe.getAction().getChannel().getKeyword())
			{
				case "gmail":
				{					
					String to = "", subject = "", body = "";
					
					for (ActionIngredient ai : actionIngredients)
					{
						ParametersActions param = ai.getParam();
						
						if (param.getKeyword().equals("to_address")) {
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
						//TODO da fare solo quando specificato in fase di creazione della ricetta
						//TODO inserire anche altre cose tipo l'utente che ha twittato etc
						body = twitterUtil.addTriggeredTweetToAction(tweet, body);
						
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
					String tweetAction = "", hashtagAction = "";
					
					for (ActionIngredient ai : actionIngredients)
					{
						ParametersActions param = ai.getParam();
						
						if (param.getKeyword().equals("tweet")) {
							tweetAction = ai.getValue();
						}
						else if (param.getKeyword().equals("hashtag")) {
							hashtagAction = ai.getValue();
						}
					}
					
					for (Status tweet : newUsefulTweets)
					{
						//TODO da fare solo quando specificato in fase di creazione della ricetta
						//TODO inserire anche altre cose tipo l'utente che ha twittato etc
						tweetAction = twitterUtil.addTriggeredTweetToAction(tweet, tweetAction);
						twitterUtil.postTweet(user.getId(), authAction, tweetAction, hashtagAction);
					}
					
					break;
				}
			}
		}
	}
}
