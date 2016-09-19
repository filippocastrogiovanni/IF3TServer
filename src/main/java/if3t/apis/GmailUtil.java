package if3t.apis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import if3t.exceptions.InvalidParametersException;
import if3t.models.Authorization;
import if3t.models.ChannelStatus;
import if3t.models.ParametersTriggers;
import if3t.models.Recipe;
import if3t.models.TriggerIngredient;
import if3t.services.ChannelStatusService;

@Component
public class GmailUtil {

	private static final JsonFactory JSON_FACTORY =
	        JacksonFactory.getDefaultInstance();

	private static HttpTransport HTTP_TRANSPORT = 
	    	new NetHttpTransport();
	
	@Autowired
    private ChannelStatusService channelStatusService;
    
	public List<Message> checkEmailReceived(Authorization auth, List<TriggerIngredient> triggerIngredients, Long timestamp, Recipe recipe) throws IOException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(auth.getAccessToken());
		Gmail gmail = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
						.setApplicationName("IF3T")
						.build();
		
		StringBuilder q = new StringBuilder();
		for(TriggerIngredient triggerIngredient: triggerIngredients){
			ParametersTriggers param = triggerIngredient.getParam();
			q.append(param.getKeyword() + ":" + triggerIngredient.getValue());
		}
		
		
		ChannelStatus channelStatus = channelStatusService.readChannelStatusByRecipeId(recipe.getId());
		
		ListMessagesResponse messageList = new ListMessagesResponse();
		
		if(channelStatus.getPageToken() == null){
			q.append(" after:" + timestamp/1000);
			messageList = gmail.users()
								.messages()
								.list("me")
								.setQ(q.toString())
								.execute();
		}
		else{
			messageList = gmail.users()
								.messages()
								.list("me")
								.setPageToken(channelStatus.getPageToken())
								.setQ(q.toString())
								.execute();
		}
		
		if(messageList.getNextPageToken() != null)
			channelStatus.setPageToken(messageList.getNextPageToken());
		
		timestamp += 1000*60*5;
		channelStatus.setSinceRef(timestamp);
		channelStatusService.updateChannelStatus(channelStatus);
		
		List<Message> messages = messageList.getMessages();
		
		return messages;
	}
	
	public String sendEmail( String to, String subject, String body, Authorization auth) throws MessagingException, IOException, URISyntaxException, InvalidParametersException{
		RestTemplate restTemplate = new RestTemplate();
		
		if(to == null)
			throw new InvalidParametersException("Address cannot be null!");
		
		Message email = createEmail(to, null, subject == null? "" : subject, body == null? "" : body);

		String ReqBody = "{\"raw\":\"" + email.getRaw() +"\"}";
		MediaType mediaType = new MediaType("application", "json");

		RequestEntity<String> request = RequestEntity
				.post(new URI("https://www.googleapis.com/gmail/v1/users/me/messages/send"))
				.contentLength(email.getRaw().getBytes().length)
				.contentType(mediaType)
				.header("Authorization", auth.getTokenType() + " " + auth.getAccessToken())
				.body(ReqBody);

		ResponseEntity<String> messageResponse = restTemplate.exchange(request, String.class);
		return messageResponse.getBody();
	}
	
	private Message createEmail(String to, String from, String subject, String bodyText) throws MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		
		MimeMessage email = new MimeMessage(session);
		
		//email.setFrom(new InternetAddress(from));
		email.addRecipient(RecipientType.TO, new InternetAddress(to));
		email.setSubject(subject);
		email.setText(bodyText);
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
	}
}
