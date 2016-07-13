package if3t.timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        //System.out.println("1: The time is now " + dateFormat.format(new Date()));
    }
    
    @Scheduled(fixedRate = 3000)
    public void reportTime() {
    	Calendar c = Calendar.getInstance();
    	c.setTimeInMillis(c.getTimeInMillis() + 10000);
        //System.out.println("2: The time is now " + dateFormat.format(c.getTime()));
    }
}