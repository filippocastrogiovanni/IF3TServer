package if3t.apis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import if3t.models.Authorization;
import if3t.models.ChannelStatus;
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
	private Twitter getTwitterInstance(Long userId, Authorization auth)
	{
		ConfigurationBuilder conf = new ConfigurationBuilder();
		conf.setDebugEnabled(true).setOAuthConsumerKey("rLWBxF1x5DwCgMhtFzGckQytZ").
		setOAuthConsumerSecret("HYAWanoKCvBHTdw7hSjMj8LPvpbwJ2MPCADgTEuhubbgTXGDW2");
		
		return new TwitterFactory(conf.build()).getInstance(new AccessToken(auth.getAccessToken(), auth.getRefreshToken(), userId));
	}
	
	public boolean postTweet(Long userId, Authorization auth, String tweet, List<String> hashtags)
	{
		String screenName = null;
		Twitter twitter = getTwitterInstance(userId, auth);
		
		if (hashtags != null && hashtags.size() > 0)
		{
			StringBuffer sb = new StringBuffer();
			
			for (String ht : hashtags)
			{
				if (ht.startsWith("#")) sb.append(" ").append(ht);
				else sb.append(" ").append("#").append(ht);
			}
			
			tweet.concat(sb.toString());
		}
		   
		try 
		{
			twitter.updateStatus(tweet);
			screenName = twitter.getScreenName();
			logger.info("Successfully updated the status of the user @" + screenName);
			return true;
		} 
		catch (Throwable t)
		{
			logger.error("Failed to update the status of the user" + ((screenName != null && screenName.length() > 0) ? " @" + screenName : ""), t);
			return false;
		}
	}
	
	//TODO assicurarsi che venga rispettato il limit rate
	public List<Status> getNewUsefulTweets(Long userId, Authorization auth, String hashtag)
	{
		String screenName = null;
		Twitter twitter = getTwitterInstance(userId, auth);
		long lastProcessedTweetId = Long.MIN_VALUE;
		List<Status> tweetList = new ArrayList<Status>();
		ChannelStatus twitterStatus = channelStatusService.readChannelStatus(userId, "twitter");
		
		try 
		{
			screenName = twitter.getScreenName();
            Paging page = new Paging(1, 200, (twitterStatus != null) ? twitterStatus.getSinceRef() : 1);
            ResponseList<Status> statuses = twitter.getHomeTimeline(page);
            
            if (statuses.size() == 0) {
            	logger.info("There are no new tweets to inspect of the user @" + screenName);
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
            	           	
            	//FIXME togliere alla fine
                System.out.println("Id " + status.getId() + " - @" + screenName + " - " + status.getText());
                
                if (status.getId() > lastProcessedTweetId) {
            		lastProcessedTweetId = status.getId();
            	}
            }
         
            if (lastProcessedTweetId > Long.MIN_VALUE) {
            	channelStatusService.updateChannelStatus(twitterStatus.getId(), lastProcessedTweetId);
            }

            return tweetList;
        } 
		catch (Throwable t)
		{
			logger.error("Failed to inspect new tweets of the user" + ((screenName != null && screenName.length() > 0) ? " @" + screenName : ""), t);
			return Collections.emptyList();
		}
	}
}