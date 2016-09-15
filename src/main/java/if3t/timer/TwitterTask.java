package if3t.timer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import if3t.apis.TwitterUtil;
import twitter4j.Status;

public class TwitterTask 
{
	@Autowired
	private TwitterUtil twitterUtil;
	
	@Scheduled(fixedRate = 1000*60*5)
    public void twitterScheduler() 
	{
    	List<Status> tweets = twitterUtil.getNewUsefulTweets(10L, null);
	}
}
