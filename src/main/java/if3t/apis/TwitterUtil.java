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
import if3t.services.AuthorizationService;
import if3t.services.ChannelStatusService;
import twitter4j.HashtagEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

@Component
public class TwitterUtil 
{	
	@Autowired
	private ChannelStatusService channelStatusService;
	@Autowired
	private AuthorizationService authorizationService;
	private final Logger logger = LoggerFactory.getLogger(TwitterUtil.class.getCanonicalName());
	
	//FIXME settare il debug a false alla fine
	private Twitter getTwitterInstance(Long userId)
	{
		ConfigurationBuilder conf = new ConfigurationBuilder();
		conf.setDebugEnabled(true).setOAuthConsumerKey("rLWBxF1x5DwCgMhtFzGckQytZ").
		setOAuthConsumerSecret("HYAWanoKCvBHTdw7hSjMj8LPvpbwJ2MPCADgTEuhubbgTXGDW2");
		
		Authorization auth = authorizationService.getAuthorization(userId, "twitter");
		return new TwitterFactory(conf.build()).getInstance(new AccessToken(auth.getAccessToken(), auth.getRefreshToken(), userId));
	}
	
	public boolean postTweet(Long userId, String tweet, List<String> hashtags)
	{
		Twitter twitter = getTwitterInstance(userId);
		
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
			logger.info("Successfully updated the status of the user @" + twitter.getScreenName());
			return true;
		} 
		catch (Throwable t)
		{
			logger.error("Failed to update the status of the user", t);
			return false;
		}
	}
	
	//TODO assicurarsi che venga rispettato il limit rate
	public List<Status> getNewUsefulTweets(Long userId, String hashtag)
	{
		Twitter twitter = getTwitterInstance(userId);
		long lastProcessedTweetId = Long.MIN_VALUE;
		List<Status> tweetList = new ArrayList<Status>();
		ChannelStatus twitterStatus = channelStatusService.readChannelStatus(userId, "twitter");
		
		try 
		{
            Paging page = new Paging(1, 200, (twitterStatus != null) ? twitterStatus.getSinceRef() : 1);
            ResponseList<Status> statuses = twitter.getHomeTimeline(page);
            
            if (statuses.size() == 0) {
            	logger.info("There are no new tweets to process of the user @" + twitter.getScreenName());
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
                System.out.println("Id " + status.getId() + " - @" + status.getUser().getScreenName() + " - " + status.getText());
                
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
			logger.error("Failed to inspect new tweets", t);
			return Collections.emptyList();
		}
	}
}