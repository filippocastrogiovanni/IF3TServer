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

		Long margin = 5L;// is the number of minutes (the same used in the
							// annotations)
		Calendar now = Calendar.getInstance();
		Long timestamp = (now.getTimeInMillis() - (margin * 60 * 1000)) / 1000;
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
							log.info("GMail refresh: POST response error in authorization id {}", auth.getId());
						else {
							auth.setAccessToken(googleRS.getAccess_token());
							auth.setExpireDate(googleRS.getExpiration_date());
							auth.setTokenType(googleRS.getToken_type());
							channelService.refreshChannelAuthorization(auth);
							log.info("GMail refresh: authorization id {} succesfully refreshed", auth.getId());
						}						

					} catch (URISyntaxException e) {
						log.info("GMail refresh: POST request error in authorization id {}", auth.getId());
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

}
