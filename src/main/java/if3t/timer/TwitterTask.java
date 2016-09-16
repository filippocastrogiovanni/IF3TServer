package if3t.timer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import if3t.apis.TwitterUtil;
import if3t.models.Authorization;
import if3t.models.Channel;
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
    	//List<Status> tweets = twitterUtil.getNewUsefulTweets(10L, "#if3t");
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
			
			switch (recipe.getAction().getChannel().getKeyword())
			{
				case "gmail":
				{
					//List<TriggerIngredient> trigIngredients = recipe.
				}
			}
		}
	}
}
