package if3t.timer;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import if3t.models.Authorization;
import if3t.services.ChannelService;

@Component
public class RefreshTokens {
	
	@Autowired
	private ChannelService channelService;
	
	@Scheduled(fixedRate = 1*60*1000)
    public void gmailTokensRefresh() {
		Long margin = 1L;
        Calendar now = Calendar.getInstance();
        Long timestamp = (now.getTimeInMillis()-(margin*60*1000))/1000;
        List<Authorization> tokens = channelService.readExpiringAuthorizations("gmail", timestamp);
        if(tokens != null) {
        	if(!tokens.isEmpty()) {
        		//TODO fare richieste a google di 
        	}
        }
    }

}
