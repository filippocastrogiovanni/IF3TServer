package if3t.timer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import if3t.models.ParametersTriggers;
import if3t.models.Recipe;
import if3t.models.TriggerIngredient;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

@Component
public class ScheduledTasks {

	@Autowired
    private RecipeService recipeService;
	@Autowired
	private TriggerIngredientService triggerIngredientService;

    @Scheduled(fixedRate = 1000*60*5)
    public void gmailScheduler() {
       List<Recipe> gmailTriggerRecipes = recipeService.getRecipeByTriggerChannel("gmail");
       for(Recipe recipe: gmailTriggerRecipes){
    	   List<TriggerIngredient> triggerIngredients = triggerIngredientService.getRecipeTriggerIngredients(recipe.getId());
    	   for(TriggerIngredient triggerIngredient: triggerIngredients){
    		   ParametersTriggers param = triggerIngredient.getParam();
    		   //TODO everything
    	   }
       }
    }
    
    public static List<Message> listMessagesMatchingQuery(Gmail service, String userId,
    	      String query) throws IOException {
    	    ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

    	    List<Message> messages = new ArrayList<Message>();
    	    while (response.getMessages() != null) {
    	      messages.addAll(response.getMessages());
    	      if (response.getNextPageToken() != null) {
    	        String pageToken = response.getNextPageToken();
    	        response = service.users().messages().list(userId).setQ(query)
    	            .setPageToken(pageToken).execute();
    	      } else {
    	        break;
    	      }
    	    }

    	    for (Message message : messages) {
    	      System.out.println(message.toPrettyString());
    	    }

    	    return messages;
    	  }

    
}