package if3t.apis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.model.Message;

import if3t.exceptions.InvalidParametersException;
import if3t.models.Authorization;

@Component
public class GmailUtil {

	public String sendEmail( String to, String subject, String body, Authorization auth) throws MessagingException, IOException, URISyntaxException, InvalidParametersException{
		RestTemplate restTemplate = new RestTemplate();
		
		if(to == null)
			throw new InvalidParametersException("Address cannot be null!");
		
		Message email = createEmail(to, "", subject == null? "" : subject, body == null? "" : body);

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
