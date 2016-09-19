package if3t.apis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import if3t.entities.Authorization;
import if3t.entities.ChannelStatus;
import if3t.services.ChannelStatusService;
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
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
	
	//FIXME settare il debug a false alla fine
	//TODO forse questa funzione va richiamata in dei try/catch (ad Andrea è uscita una nullpointerexception)
	private Twitter getTwitterInstance(Long userId, Authorization auth)
	{
		ConfigurationBuilder conf = new ConfigurationBuilder();
		conf.setDebugEnabled(true).setOAuthConsumerKey("rLWBxF1x5DwCgMhtFzGckQytZ").
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
	
	//TODO assicurarsi che venga rispettato il limit rate
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
            	else {
            		channelStatusService.updateChannelStatus(twitterStatus.getId(), lastProcessedTweetIdByRecipe);
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
	
	public String addTriggeredTweetToAction(Status tweet, String message)
	{
		StringBuffer sb = new StringBuffer(message);
		sb.append("\n------------------------------------------------------------");
		sb.append("\nFrom: @" + tweet.getUser().getScreenName());
		sb.append("\nCreated at: " + tweet.getCreatedAt().toString());
		sb.append("\nContent of the tweet:\n\n" + tweet.getText());
		return sb.toString();
	}
}