package if3t.apis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import if3t.entities.Authorization;
import if3t.entities.ChannelStatus;
import if3t.entities.TriggerIngredient;
import if3t.services.ChannelStatusService;
import if3t.services.CreateRecipeService;
import twitter4j.HashtagEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

@Component
public class TwitterUtil 
{	
	@Autowired
	private ChannelStatusService channelStatusService;
	@Autowired
	private CreateRecipeService createRecipeService;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
	
	private Twitter getTwitterInstance(Long userId, Authorization auth)
	{
		ConfigurationBuilder conf = new ConfigurationBuilder();
		conf.setDebugEnabled(false).setOAuthConsumerKey("rLWBxF1x5DwCgMhtFzGckQytZ").
		setOAuthConsumerSecret("HYAWanoKCvBHTdw7hSjMj8LPvpbwJ2MPCADgTEuhubbgTXGDW2");
		
		return new TwitterFactory(conf.build()).getInstance(new AccessToken(auth.getAccessToken(), auth.getRefreshToken(), userId));
	}
	
	public boolean postTweet(Long userId, Authorization auth, String tweet, String hashtag)
	{
		String screenName = null;
		Twitter twitter = getTwitterInstance(userId, auth);
		
		if (hashtag != null && hashtag.length() > 0) {
			tweet.concat((hashtag.startsWith("#") ? " " : " #") + hashtag);
		}
		   
		try 
		{
			twitter.updateStatus(tweet);
			screenName = twitter.getScreenName();
			logger.info("Successfully updated the Twitter status of the user @" + screenName);
			return true;
		} 
		catch (Throwable t)
		{
			logger.error("Failed to update the Twitter status of the user" + ((screenName != null && screenName.length() > 0) ? " @" + screenName : ""), t);
			return false;
		}
	}
	
	public List<Status> getNewUsefulTweets(Long userId, Long recipeId, Authorization auth, String hashtag, String fromUser)
	{
		String screenName = null;
		Twitter twitter = getTwitterInstance(userId, auth);
		long lastProcessedTweetIdByRecipe = Long.MIN_VALUE;
		List<Status> tweetList = new ArrayList<Status>();
		ChannelStatus twitterStatus = channelStatusService.readChannelStatusByRecipeId(recipeId);
		
		try 
		{
			screenName = (fromUser != null && fromUser.length() > 0 ) ? fromUser : twitter.getScreenName();
            Paging page = new Paging(1, 200, (twitterStatus != null) ? twitterStatus.getSinceRef() : 1);
            ResponseList<Status> statuses = (fromUser != null && fromUser.length() > 0 ) ? twitter.getUserTimeline(fromUser, page) : twitter.getUserTimeline(page);
            
            if (statuses.size() == 0) 
            {
            	logger.info("There are no new tweets of the user @" + screenName + " to inspect");
            	return Collections.emptyList();
            }
            
            for (Status status : statuses) 
            {
            	if (hashtag != null && hashtag.length() > 0)
            	{
            		if (hashtag.startsWith("#")) {
            			hashtag = hashtag.substring(1);
            		}
            		
            		for (HashtagEntity hte : status.getHashtagEntities())
                    {
                    	if (hashtag.equalsIgnoreCase(hte.getText())) 
                    	{
                    		tweetList.add(status);
                    		break;
                    	}
                    }
            	}
            	else
            	{
            		tweetList.add(status);
            	}
                
                if (status.getId() > lastProcessedTweetIdByRecipe) {
            		lastProcessedTweetIdByRecipe = status.getId();
            	}
            }
         
            if (lastProcessedTweetIdByRecipe > Long.MIN_VALUE) 
            {
            	if (twitterStatus == null) {
            		channelStatusService.createNewChannelStatus(recipeId, lastProcessedTweetIdByRecipe);
            	}
            	else 
            	{
            		twitterStatus.setSinceRef(lastProcessedTweetIdByRecipe);
            		channelStatusService.updateChannelStatus(twitterStatus);
            	}
            }

            return tweetList;
        } 
		catch (Throwable t)
		{
			logger.error("Failed to inspect new tweets of the user" + ((screenName != null && screenName.length() > 0) ? " @" + screenName : ""), t);
			return Collections.emptyList();
		}
	}
	
	private String printTriggeredTweet(Status tweet)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("\n------------------------------------------------------------");
		sb.append("\nFrom: @" + tweet.getUser().getScreenName());
		sb.append("\nCreated at: " + tweet.getCreatedAt().toString());
		sb.append("\nContent of the tweet:\n\n" + tweet.getText());
		sb.append("\n------------------------------------------------------------");
		return sb.toString();
	}
	
	public String replaceKeywords(String text, Long triggerId, List<TriggerIngredient> trigIngrList, Status tweet, int maxLength)
	{
		String target;
		Set<String> validKeywords = createRecipeService.readChannelKeywords(triggerId, "twitter");
		
		for (String vk : validKeywords)
		{
			target = "[" + vk + "]";
			
			for (TriggerIngredient ti: trigIngrList)
			{
				if (ti.getParam().getKeyword().equals(vk))
				{
					text = text.replace(target, !vk.equals("tweet") ? ti.getValue() : printTriggeredTweet(tweet));
					break;
				}
			}
		}
		
		return (maxLength < 0 || text.length() <= maxLength) ? text : text.substring(0, maxLength - 4).concat(" ...");
	}
}