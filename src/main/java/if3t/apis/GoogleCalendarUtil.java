package if3t.apis;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import if3t.exceptions.InvalidParametersException;
import if3t.models.Authorization;
import if3t.models.GCalendarDatePojo;
import if3t.models.GCalendarEventPOJO;

public class GoogleCalendarUtil {

	public static boolean createEvent(Calendar startDate, Calendar endDate, String title, String description, String location, Authorization auth) throws InvalidParametersException, JsonProcessingException, URISyntaxException{
		if(startDate == null || endDate == null)
			throw new InvalidParametersException("startDate and endDate are required fields and can not be null!");
		
		String start = createUsableDateTime(startDate);
		String end = createUsableDateTime(endDate);
		String body = createJsonBody(start, end, title, location, description);
		
		RestTemplate restTemplate = new RestTemplate();
		MediaType mediaType = new MediaType("application", "json");

		RequestEntity<String> request = RequestEntity
				.post(new URI("https://www.googleapis.com/calendar/v3/calendars/primary/events"))
				.contentLength(body.getBytes().length)
				.contentType(mediaType)
				.header("Authorization", auth.getTokenType() + " " + auth.getAccessToken())
				.body(body);

		ResponseEntity<String> messageResponse = restTemplate.exchange(request, String.class);
		return messageResponse.getStatusCode().is2xxSuccessful()? true : false;
	}
	
	

	private static String createJsonBody(String start, String end, String title, String location, String description) throws JsonProcessingException {
		GCalendarDatePojo startPOJO = new GCalendarDatePojo();
		startPOJO.setDateTime(start);
		GCalendarDatePojo endPOJO = new GCalendarDatePojo();
		endPOJO.setDateTime(end);
		
		GCalendarEventPOJO eventPOJO = new GCalendarEventPOJO();
		eventPOJO.setStart(startPOJO);
		eventPOJO.setEnd(endPOJO);
		eventPOJO.setTitle(title);
		eventPOJO.setLocation(location);
		eventPOJO.setDescription(description);
		
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(eventPOJO);
	}

	private static String createUsableDateTime(Calendar startDate) {
		StringBuilder startBuilder = new StringBuilder();
		startBuilder.append(startDate.get(Calendar.YEAR));
		startBuilder.append("-");
		startBuilder.append(startDate.get(Calendar.MONTH) + 1);
		startBuilder.append("-");
		startBuilder.append(startDate.get(Calendar.DAY_OF_MONTH));
		startBuilder.append("T");
		startBuilder.append(startDate.get(Calendar.HOUR_OF_DAY));
		startBuilder.append(":");
		startBuilder.append(startDate.get(Calendar.MINUTE));
		startBuilder.append(":");
		startBuilder.append(startDate.get(Calendar.SECOND));
		int timeZone = startDate.get(Calendar.ZONE_OFFSET)/60/60/1000;
		startBuilder.append(timeZone > 0? "+" + timeZone : timeZone);
		startBuilder.append(":00");
		
		return startBuilder.toString();
	}
}
