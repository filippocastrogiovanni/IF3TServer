package if3t.apis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;

import if3t.models.Authorization;
import if3t.models.ChannelStatus;
import if3t.services.AuthorizationService;
import if3t.services.ChannelStatusService;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterUtil 
{	
	@Autowired
	private ChannelStatusService channelStatusService;
	@Autowired
	private AuthorizationService authorizationService;
	
	//TODO probabilmente c'è da scontrarsi con il fatto che l'istanza twitter non è singleton così
	//FIXME settare io debug a false
	public Twitter getTwitterInstance(Long userId)
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
			Status status = twitter.updateStatus(tweet);
			System.out.println("@" + status.getUser().getScreenName() + " - Successfully updated the status to [" + status.getText() + "].");
			return true;
		} 
		catch (TwitterException te) 
		{
			System.err.println("Failed to update the status of the user");
			System.err.println("---------------------------------------------------------------");
            te.printStackTrace();
			return false;
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			return false;
		}
	}
	
//	public boolean isNewTweetPosted(Long userId)
//	{
//		Twitter twitter = getTwitterInstance(userId);
//		ChannelStatus twitterStatus = channelStatusService.readChannelStatus(userId, "twitter");
//		
//		if (twitterStatus != null)
//		{
//			
//		}
//		
//		try 
//		{
//            Paging page = new Paging(1, 200, sinceRef);
//            ResponseList<Status> statuses = twitter.getHomeTimeline(page);
//            
//            while (statuses.size() > 0)
//            {
////                System.out.println(statuses.getRateLimitStatus().getLimit());
//                System.out.println(statuses.getRateLimitStatus().getRemaining());
////                System.out.println(statuses.getRateLimitStatus().getResetTimeInSeconds());
//                System.out.println(statuses.getRateLimitStatus().getSecondsUntilReset());
//                
//                for (Status status : statuses) 
//                {
//                    System.out.println("page " + page.getPage() + " - Id " + status.getId() + " - @" + status.getUser().getScreenName() + " - " + status.getText());
//                }
//                
//                //TODO potrebbe esplodere quello fuori dal while, ma se il controllo viene fatto ogni 15 minuti non dovrebbe accadere
//                if (statuses.getRateLimitStatus().getRemaining() > 0)
//                {
//	                page.setPage(page.getPage() + 1);
//	                statuses = twitter.getHomeTimeline(page);
//                }
//                else
//                {
//                	//TODO salvare sinceId e maxId
//                	System.out.println("Limite raggiunto");
//                	break;
//                }  
//            } 
//            
//            System.out.println("done.");
//        } 
//		catch (TwitterException te) 
//		{
//            System.err.println("Failed to list statuses: " + te.getMessage());
//            System.err.println("---------------------------------------------------------------");
//            te.printStackTrace();
//        }
//		catch (Throwable t)
//		{
//			t.printStackTrace();
//		}
//		finally
//		{
//			//TODO se sinceId e maxId sono != null occorre salvarli nel db
//		}
//	}
}