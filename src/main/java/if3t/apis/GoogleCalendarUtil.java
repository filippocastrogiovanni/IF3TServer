package if3t.apis;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.List;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.*;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import if3t.exceptions.InvalidParametersException;
import if3t.models.Authorization;
import if3t.models.ChannelStatus;
import if3t.models.GCalendarDatePojo;
import if3t.models.GCalendarEventPOJO;
import if3t.models.User;
import if3t.services.ChannelStatusService;

@Component
public class GoogleCalendarUtil {

    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    private static HttpTransport HTTP_TRANSPORT = 
    	new NetHttpTransport();
    
    @Autowired
    private ChannelStatusService channelStatusService;
    
	public boolean createEvent(Calendar startDate, Calendar endDate, String title, String description, String location, Authorization auth) throws InvalidParametersException, JsonProcessingException, URISyntaxException{
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
	
	public List<Event> isEventAdded(Authorization auth, User user) throws IOException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(auth.getAccessToken());
		com.google.api.services.calendar.Calendar calendar = 
				 new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				 .setApplicationName("IF3T")
				 .build();
		
		ChannelStatus channelStatus = channelStatusService.readChannelStatus(user.getId(), "gcalendar");
		
		Long timestamp = 0L;
		if(channelStatus == null)
			timestamp = Calendar.getInstance().getTimeInMillis() - (1000*60*5);
		else
			timestamp = channelStatus.getSinceRef();
		
		DateTime dateMin = new DateTime(timestamp/*1473976800000l*/);
		Events events = null;
		if(channelStatus.getPageToken() == null){
			events = calendar.events()
							.list("primary")
							.setMaxResults(50)
							.setSingleEvents(true)
							.setTimeMin(dateMin)
							.execute();
		}
		else{
			events = calendar.events()
							.list("primary")
							.setMaxResults(50)
							.setPageToken(channelStatus.getPageToken())
							.setSingleEvents(true)
							.setTimeMin(dateMin)
							.execute();
		}
		
		if(events.getNextPageToken() != null)
			channelStatus.setPageToken(events.getNextPageToken());
		
		timestamp += 1000*60*5;
		channelStatus.setSinceRef(timestamp);
		
		channelStatusService.updateChannelStatus(channelStatus);
		
		return events.getItems();
		/*List<Event> items = events.getItems();
        if (items.size() == 0) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }*/
		/*RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", auth.getTokenType() + " " + auth.getAccessToken());
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

		String url = "https://www.googleapis.com/calendar/v3/calendars/primary/events?q=after:1325376000&maxResults=10&orderBy=startTime&singleEvents=true";
		HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		System.out.println(response.getBody());*/
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
