package if3t.timer;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import if3t.apis.FacebookUtil;
import if3t.apis.GmailUtil;
import if3t.apis.GoogleCalendarUtil;
import if3t.entities.ActionIngredient;
import if3t.entities.Authorization;
import if3t.entities.Channel;
import if3t.entities.ParametersActions;
import if3t.entities.ParametersKeyword;
import if3t.entities.ParametersTriggers;
import if3t.entities.Recipe;
import if3t.entities.TriggerIngredient;
import if3t.entities.User;
import if3t.repositories.ParametersTriggersRepository;
import if3t.services.ActionIngredientService;
import if3t.services.AuthorizationService;
import if3t.services.ChannelStatusService;
import if3t.services.ParameterKeywordService;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;
import java.util.ArrayList;

@Component
public class FacebookTask {

	@Autowired
	private FacebookUtil facebookUtil;
	@Autowired
    private RecipeService recipeService;
	@Autowired
	private TriggerIngredientService triggerIngredientService;
	@Autowired
	private ParameterKeywordService parametersKeywordService;
	@Autowired
	private ActionIngredientService actionIngredientService;
	@Autowired
	private AuthorizationService authService;
	@Autowired
	private ChannelStatusService channelStatusService;	
	@Autowired
	private ParametersTriggersRepository parametersTriggerRepository;

	
	private ConcurrentHashMap<String, String> couples_access_token_full_names = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> couples_access_token_profile_pictures = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> couples_access_token_locations = new ConcurrentHashMap<String, String>();

	
	@Scheduled(fixedRateString = "${app.scheduler.value}")
	public void gmailScheduler(){
	
		List<Recipe> facebookTriggerRecipes = recipeService.getEnabledRecipesByTriggerChannel("facebook");
		for(Recipe recipe: facebookTriggerRecipes){
			try{
				User user = recipe.getUser();
				Channel triggerChannel = recipe.getTrigger().getChannel();
				Channel actionChannel = recipe.getAction().getChannel();
				Authorization triggerAuth = authService.getAuthorization(user.getId(), triggerChannel.getKeyword());
				Authorization actionAuth = authService.getAuthorization(user.getId(), actionChannel.getKeyword());
				
				//Checking if the access token of the trigger channel is expired
				Calendar now = Calendar.getInstance();
				if(triggerAuth == null || triggerAuth.getExpireDate()*1000 <= now.getTimeInMillis()){
					continue;
				}

				//Checking if the access token of the action channel is not present
				if(actionAuth == null)
					continue;
				
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
				
				switch(situation){
				//the server can not wait for the verification of all the conditions because they could never happen, so we just take into account a moment, if in that moment the conditions are verified, then we proceed with the action
				case 0://new post
					try {
						new_posts = facebookUtil.calculate_new_posts_by_user_number(access_token, recipe.getId());
					} catch (Exception e) {
						System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
						e.printStackTrace();
					}	
				   	if(new_posts.size()<=0)
				   		continue; 	
					break;
				case 1://every one
				   	   //TO DO substitute the following token with the real user one
				   	   try {
							full_name_changed = facebookUtil.is_full_name_changed(access_token, couples_access_token_full_names, recipe.getId());
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}		
					   	try {
							profile_picture_changed = facebookUtil.is_profile_picture_changed(access_token, couples_access_token_profile_pictures, recipe.getId());
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}
					   	try {
							location_changed = facebookUtil.is_location_changed(access_token, couples_access_token_locations, recipe.getId());;
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}
					   	if(!(full_name_changed&&profile_picture_changed&&location_changed))
					   		continue;
						break;
				case 2://full_name
						try {
							full_name_changed = facebookUtil.is_full_name_changed(access_token, couples_access_token_full_names, recipe.getId());
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}	
					   	if(!(full_name_changed))
					   		continue;
						break;
				case 3://profile_picture
						try {
							profile_picture_changed = facebookUtil.is_profile_picture_changed(access_token, couples_access_token_profile_pictures, recipe.getId());
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}
					   	if(!(profile_picture_changed))
					   		continue;
						break;
				case 4://location
						try {
							location_changed = facebookUtil.is_location_changed(access_token, couples_access_token_locations, recipe.getId());
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}
					   	if(!(location_changed))
					   		continue;					
						break;
				case 5://full_name, profile_picture
						try {
							full_name_changed = facebookUtil.is_full_name_changed(access_token, couples_access_token_full_names, recipe.getId());
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}	
						try {
							profile_picture_changed = facebookUtil.is_profile_picture_changed(access_token, couples_access_token_profile_pictures, recipe.getId());
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}
					   	if(!(full_name_changed&&profile_picture_changed))
					   		continue;
						break;
				case 6://full_name, location
						try {
							full_name_changed = facebookUtil.is_full_name_changed(access_token, couples_access_token_full_names, recipe.getId());
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}		
						try {
							location_changed = facebookUtil.is_location_changed(access_token, couples_access_token_locations, recipe.getId());
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}
					   	if(!(full_name_changed&&location_changed))
					   		continue;
						break;		
				case 7://profile_picture, location
						try {
							profile_picture_changed = facebookUtil.is_profile_picture_changed(access_token, couples_access_token_profile_pictures, recipe.getId());
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}		
						try {
							location_changed = facebookUtil.is_location_changed(access_token, couples_access_token_locations, recipe.getId());
						} catch (Exception e) {
							System.out.println("A server error occurred (very likely a JSONException due to the fact that the user removed the needed permission.");
							e.printStackTrace();
						}
					   	if(!(profile_picture_changed&&location_changed))
					   		continue;
						break;	
				}
				
				//here the trigger has been happened, so we can proceed with the action
				//TODO add action management when is ready
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
	
							//there are no other possible actions
							//if(param.getKeyword().equals("post"))
								message = actionIngredient.getValue();
						}
						facebookUtil.publish_new_post(message, actionAuth.getAccessToken());
						break;
					
					}
				}
				else{
					for(String s : new_posts){
						
						switch(recipe.getAction().getChannel().getKeyword()){
						
						case "facebook" :
							String message = "";
							for(ActionIngredient actionIngredient: actionIngredients){
								ParametersActions param = actionIngredient.getParam();
		
								//there are no other possible actions
								//if(param.getKeyword().equals("post"))
									message = actionIngredient.getValue();
							}
							facebookUtil.publish_new_post(message, actionAuth.getAccessToken());
							break;
						}
					}	
				}
				
			}catch (Exception e){
				e.printStackTrace();
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

