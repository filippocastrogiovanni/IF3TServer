package if3t.apis;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import if3t.exceptions.InvalidParametersException;
import if3t.models.Authorization;
import if3t.models.ChannelStatus;
import if3t.models.GCalendarDatePojo;
import if3t.models.GCalendarEventPOJO;
import if3t.models.Recipe;
import if3t.services.ChannelStatusService;

@Component
public class GoogleCalendarUtil {

    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    private static HttpTransport HTTP_TRANSPORT = 
    	new NetHttpTransport();
    
    @Autowired
    private ChannelStatusService channelStatusService;
    
	public boolean createEvent(Calendar startDate, Calendar endDate, String title, String description, String location, Authorization auth) throws InvalidParametersException, URISyntaxException, IOException{
		if(startDate == null || endDate == null)
			throw new InvalidParametersException("startDate and endDate are required fields and can not be null!");
		
		GoogleCredential credential = new GoogleCredential().setAccessToken(auth.getAccessToken());
		com.google.api.services.calendar.Calendar calendar = 
				 new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				 .setApplicationName("IF3T")
				 .build();
		
		EventDateTime start = new EventDateTime();
		EventDateTime end = new EventDateTime();
		start.setDateTime(new DateTime(startDate.getTimeInMillis()));
		end.setDateTime(new DateTime(endDate.getTimeInMillis()));
		
		Event event = new Event();
		event.setStart(start);
		event.setEnd(end);
		if(title != null && !title.equals(""))
			event.setSummary(title);
		if(location != null && !location.equals(""))
			event.setLocation(location);
		if(description != null && !description.equals(""))
			event.setDescription(description);
		
		HttpResponse response = calendar.events()
										.insert("primary", event)
										.executeUnparsed();
		
		return (response.getStatusCode() < 300 && response.getStatusCode()>= 200)? true : false;
		
		/*String start = createUsableDateTime(startDate);
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
		return messageResponse.getStatusCode().is2xxSuccessful()? true : false;*/
	}
	
	public List<Event> checkEventsAdded(Authorization auth, Recipe recipe, String ingredientValue) throws IOException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(auth.getAccessToken());
		com.google.api.services.calendar.Calendar calendar = 
				 new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				 .setApplicationName("IF3T")
				 .build();
		
		ChannelStatus channelStatus = channelStatusService.readChannelStatusByRecipeId(recipe.getId());
		
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
		
		//return events.getItems();
		List<Event> items = events.getItems();
        if (items.size() == 0)
            return items;

        List<Event> targetEvents = new ArrayList<>();
        for (Event event : items) {
            if(	(event.getSummary() != null && event.getSummary().contains(ingredientValue)) ||
            	(event.getDescription() != null &&event.getDescription().contains(ingredientValue)) ||
            	(event.getLocation() != null && event.getLocation().contains(ingredientValue)))
            	
            	targetEvents.add(event);
        }
        
        return targetEvents;
	}
	
	public List<Event> checkEventsStarted(Authorization auth, Recipe recipe, String ingredientValue) throws IOException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(auth.getAccessToken());
		com.google.api.services.calendar.Calendar calendar = 
				 new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				 .setApplicationName("IF3T")
				 .build();
		
		ChannelStatus channelStatus = channelStatusService.readChannelStatusByRecipeId(recipe.getId());
		
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
							.setOrderBy("startTime")
							.setSingleEvents(true)
							.setTimeMin(dateMin)
							.execute();
		}
		else{
			events = calendar.events()
							.list("primary")
							.setMaxResults(50)
							.setOrderBy("startTime")
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
		
		List<Event> items = events.getItems();
        if (items.size() == 0)
            return items;
        if(ingredientValue == null || ingredientValue.equals(""))
        	return items;

        List<Event> targetEvents = new ArrayList<>();
        for (Event event : items) {
            if(	(event.getSummary() != null && event.getSummary().contains(ingredientValue)) ||
            	(event.getDescription() != null &&event.getDescription().contains(ingredientValue)) ||
            	(event.getLocation() != null && event.getLocation().contains(ingredientValue)))
            	
            	targetEvents.add(event);
        }
        
        return targetEvents;
	}
	
	/*private static String createJsonBody(String start, String end, String title, String location, String description) throws JsonProcessingException {
		GCalendarDatePojo startPOJO = new GCalendarDatePojo();
		startPOJO.setDateTime(start);
		GCalendarDatePojo endPOJO = new GCalendarDatePojo();
		endPOJO.setDateTime(end);
		
		GCalendarEventPOJO eventPOJO = new GCalendarEventPOJO();
		eventPOJO.setStart(startPOJO);
		eventPOJO.setEnd(endPOJO);
		if(title != null && !title.equals(""))
			eventPOJO.setTitle(title);
		if(location != null && !location.equals(""))
			eventPOJO.setLocation(location);
		if(description != null && !description.equals(""))
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
	}*/
}
