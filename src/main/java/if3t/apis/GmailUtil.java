package if3t.apis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.StringUtils;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import if3t.entities.Authorization;
import if3t.entities.ChannelStatus;
import if3t.entities.ParametersTriggers;
import if3t.entities.Recipe;
import if3t.entities.TriggerIngredient;
import if3t.exceptions.InvalidParametersException;
import if3t.services.ChannelStatusService;
import if3t.services.CreateRecipeService;

@Component
public class GmailUtil {

	private static final JsonFactory JSON_FACTORY =
	        JacksonFactory.getDefaultInstance();

	private static HttpTransport HTTP_TRANSPORT = 
	    	new NetHttpTransport();
	
	@Autowired
    private ChannelStatusService channelStatusService;
	@Autowired
	private CreateRecipeService createRecipeService;
	
	@Value("${app.scheduler.value}")
	private long rate;
    
	public List<Message> getMessages(Authorization auth, List<Message> messagesToRetrieve) throws IOException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(auth.getAccessToken());
		Gmail gmail = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
						.setApplicationName("IF3T")
						.build();
		
		List<Message> messages = new ArrayList<>();
		
		for(Message message : messagesToRetrieve){
			Message targetMessage = gmail.users()
										.messages()
										.get("me", message.getId())
										.execute();
			messages.add(targetMessage);
		}
		
		return messages;
	}
	
	public List<Message> checkEmailReceived(Authorization auth, List<TriggerIngredient> triggerIngredients, Recipe recipe) throws IOException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(auth.getAccessToken());
		Gmail gmail = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
						.setApplicationName("IF3T")
						.build();
		
		StringBuilder q = new StringBuilder();
		for(TriggerIngredient triggerIngredient: triggerIngredients){
			ParametersTriggers param = triggerIngredient.getParam();
			if(param.getKeyword().equals("from_address"))
				q.append("from:" + triggerIngredient.getValue() + " ");
			else
				q.append(param.getKeyword() + ":" + triggerIngredient.getValue() + " ");
		}

		Long timestamp = 0L;
		ChannelStatus channelStatus = channelStatusService.readChannelStatusByRecipeId(recipe.getId());
		if(channelStatus == null){
			timestamp = Calendar.getInstance().getTimeInMillis() - (rate);
			channelStatus = channelStatusService.createNewChannelStatus(recipe.getId(), timestamp);
		}
		else
			timestamp = channelStatus.getSinceRef();
		
		q.append(" after:" + channelStatus.getSinceRef()/1000);
		
		 ListMessagesResponse messageList = gmail.users()
												.messages()
												.list("me")
												.setQ(q.toString())
												.execute();
		
		timestamp += rate;
		channelStatus.setSinceRef(timestamp);
		channelStatusService.updateChannelStatus(channelStatus);
		
		List<Message> messages = messageList.getMessages() == null? new ArrayList<Message>() : messageList.getMessages();
		
		return messages;
	}
	
	public boolean sendEmail( String to, String subject, String body, Authorization auth) throws MessagingException, IOException, URISyntaxException, InvalidParametersException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(auth.getAccessToken());
		Gmail gmail = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
						.setApplicationName("IF3T")
						.build();
		
		//RestTemplate restTemplate = new RestTemplate();
		
		if(to == null)
			throw new InvalidParametersException("Address cannot be null!");
		
		Message email = createEmail(to, null, subject == null? "" : subject, body == null? "" : body);

		HttpResponse response = gmail.users()
									.messages()
									.send("me", email)
									.executeUnparsed();
		response.disconnect();
		return (response.getStatusCode() < 300 && response.getStatusCode()>= 200)? true : false;
		
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
	
	public String validateAndReplaceKeywords(String ingredient, Long triggerId, int maxLength, Message message){
		String ingredientReplaced = ingredient;
		Set<String> validKeywords = createRecipeService.readChannelKeywords(triggerId, "gmail");
		int index = 0;
		
		String from = "";
		String to = "";
		String subject = "";
		String body = StringUtils.newStringUtf8(Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData()));

		for(MessagePartHeader part: message.getPayload().getHeaders()){
			if(part.getName().equals("Subject"))
				subject = part.getValue();
			if(part.getName().equals("From"))
				from = part.getValue();
			if(part.getName().equals("To"))
				to = part.getValue();
		}
		
		while(true){
			int squareOpenIndex = ingredient.indexOf('[', index);
			int squareCloseIndex = ingredient.indexOf(']', squareOpenIndex);
			
			if(squareOpenIndex == -1 || squareCloseIndex == -1){
				break;
			}
			
			index = squareCloseIndex + 1;
			
			String keyword = ingredient.substring(squareOpenIndex+1, squareCloseIndex);
			
			if(validKeywords.contains(keyword)){
				switch(keyword){
					case "from_address" :
						ingredientReplaced = ingredientReplaced.replace("[" + keyword + "]", from);
						break;
					case "to_address" :
						ingredientReplaced = ingredientReplaced.replace("[" + keyword + "]", to);
						break;
					case "subject" :
						ingredientReplaced = ingredientReplaced.replace("[" + keyword + "]", subject);
						break;
					case "body" :
						ingredientReplaced = ingredientReplaced.replace("[" + keyword + "]", body);
						break;
				}
			}
		}

		if(ingredientReplaced.length() > maxLength && (maxLength - 4) > 0)
			ingredientReplaced = ingredientReplaced.substring(0, maxLength - 4) + "...";
		
		return ingredientReplaced;
	}
}
