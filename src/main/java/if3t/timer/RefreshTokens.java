package if3t.timer;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;

import if3t.apis.GoogleRefreshTokenRequest;
import if3t.apis.GoogleRefreshTokenResponse;
import if3t.models.Authorization;
import if3t.services.ChannelService;

@Component
public class RefreshTokens {

	@Autowired
	private ChannelService channelService;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Scheduled(fixedRate = 5 * 60 * 1000)
	public void gmailTokensRefresh() {
		log.info("GMail refresh: start check");

		Long margin = 15L;
		Calendar now = Calendar.getInstance();
		Long timestamp = (now.getTimeInMillis() + (margin * 60 * 1000)) / 1000;
		List<Authorization> tokens = channelService.readExpiringAuthorizations("gmail", timestamp);
		if (tokens != null) {
			if (!tokens.isEmpty()) {
				log.info("GMail refresh: {} tokens must be refreshed", tokens.size());

				RestTemplate restTemplate = new RestTemplate();
				MediaType mediaType = new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8"));
				GoogleRefreshTokenRequest googleRQ = null;
				GoogleRefreshTokenResponse googleRS = null;
				RequestEntity<String> request = null;
				ResponseEntity<String> response = null;

				for (Authorization auth : tokens) {
					try {
						googleRQ = new GoogleRefreshTokenRequest(auth.getRefreshToken());
						request = RequestEntity.post(new URI(googleRQ.getToken_uri()))
								.contentLength(googleRQ.getRequestBody().getBytes().length).contentType(mediaType)
								.body(googleRQ.getRequestBody());
						response = restTemplate.exchange(request, String.class);
						googleRS = new GoogleRefreshTokenResponse(response.getBody());
						
						if(!googleRS.isValid())
							log.error("GMail refresh: POST response error in authorization id {}", auth.getId());
						else {
							auth.setAccessToken(googleRS.getAccess_token());
							auth.setExpireDate(googleRS.getExpiration_date());
							auth.setTokenType(googleRS.getToken_type());
							channelService.refreshChannelAuthorization(auth);
							log.info("GMail refresh: authorization id {} succesfully refreshed", auth.getId());
						}						

					} catch (URISyntaxException e) {
						log.error("GMail refresh: POST request error in authorization id {}", auth.getId());
						e.printStackTrace();
					}
				}
			} else {
				log.info("GMail refresh: no tokens to refresh");
			}
		} else {
			log.info("GMail refresh: no tokens to refresh");
		}
	}
	
	@Scheduled(fixedRate = 5 * 60 * 1000)
	public void facebookTokensRefresh() {
		log.info("Facebook refresh: start check");

		Long margin = 5L;// is the number of minutes (the same used in the
		// annotations)
		Calendar now = Calendar.getInstance();
		Long timestamp = (now.getTimeInMillis() - (margin * 60 * 1000)) / 1000;
		List<Authorization> tokens = channelService.readExpiringAuthorizations("facebook", timestamp);
		if (tokens != null) {
			if (!tokens.isEmpty()) {
				log.info("Facebook refresh: {} tokens must be refreshed", tokens.size());
				for (Authorization auth : tokens) {
					String old_token = auth.getAccessToken();
					FacebookClient fbClient = new DefaultFacebookClient(old_token);
					AccessToken exAccessToken = fbClient.obtainExtendedAccessToken("1664045957250331", "4489952bdf1294fe0fd156288a2602f4"); //appId, appSecret
					if(exAccessToken!=null){
						System.out.println("The token access " + exAccessToken.getAccessToken() + " expires on" + exAccessToken.getExpires());
						auth.setAccessToken(exAccessToken.getAccessToken());
						Calendar c = Calendar.getInstance();
						c.setTime(exAccessToken.getExpires());
						auth.setExpireDate(c.getTimeInMillis()/1000);
						channelService.refreshChannelAuthorization(auth);
						log.info("Facebook refresh: authorization id {} succesfully refreshed", auth.getId());
					}
					else{
						log.error("Facebook refresh: authorization id {} succesfully refreshed", auth.getId());
					}
				}
			} else {
				log.info("Facebook refresh: no tokens to refresh");
			}
		} else {
			log.info("Facebook refresh: no tokens to refresh");
		}
	}

}
