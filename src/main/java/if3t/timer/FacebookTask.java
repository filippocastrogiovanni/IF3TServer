package if3t.timer;

import java.util.concurrent.ConcurrentHashMap;

public class FacebookTask {

	//hashmaps with one row for each user who has this trigger
		private ConcurrentHashMap<String, String> couples_access_token_full_names = new ConcurrentHashMap<String, String>();
		private ConcurrentHashMap<String, String> couples_access_token_profile_pictures = new ConcurrentHashMap<String, String>();
		private ConcurrentHashMap<String, String> couples_access_token_locations = new ConcurrentHashMap<String, String>();

	    /*
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
}
