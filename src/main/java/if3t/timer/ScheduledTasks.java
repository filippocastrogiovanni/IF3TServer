package if3t.timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;

import if3t.services.ChannelService;

@Component
public class ScheduledTasks {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    

    @Autowired
	private ChannelService channelService;
/*
    @Scheduled(fixedRate = 5*60*1000)
    public void reportCurrentTime() {
        //System.out.println("1: The time is now " + dateFormat.format(new Date()));
    	String accessToken = channelService.readUserChannelAuthorization(9L, "facebook");
    	FacebookClient fbClient = new DefaultFacebookClient(accessToken);
    	FacebookType publishMessageResponse= fbClient.publish("me/feed", FacebookType.class,
    	         Parameter.with("message", "Try to post"));
    	System.out.println("eseguito ora");
    }
    */
//    
//    @Scheduled(fixedRate = 3000)
//    public void reportTime() {
//    	Calendar c = Calendar.getInstance();
//    	c.setTimeInMillis(c.getTimeInMillis() + 10000);
//        System.out.println("2: The time is now " + dateFormat.format(c.getTime()));
//    }
}