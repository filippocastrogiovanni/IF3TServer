package if3t.apis;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

import if3t.entities.Authorization;
import if3t.entities.ChannelStatus;
import if3t.entities.Recipe;
import if3t.exceptions.InvalidParametersException;
import if3t.services.ChannelStatusService;
import if3t.services.CreateRecipeService;

@Component
public class GoogleCalendarUtil {

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
    
	public boolean createEvent(Calendar startDate, Calendar endDate, String title, String description, String location, Authorization auth) throws InvalidParametersException, URISyntaxException, IOException{
		if(startDate == null || endDate == null)
			throw new InvalidParametersException("ERROR: startDate and endDate are required fields and can not be null!");
		if(startDate.after(endDate))
			throw new InvalidParametersException("ERROR: startDate can't be after endDate!");

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
		
		response.disconnect();
		return (response.getStatusCode() < 300 && response.getStatusCode()>= 200)? true : false;
	}
	
	public List<Event> checkEventsAdded(Authorization auth, Recipe recipe, String ingredientValue) throws IOException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(auth.getAccessToken());
		com.google.api.services.calendar.Calendar calendar = 
				 new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				 .setApplicationName("IF3T")
				 .build();
		
		ChannelStatus channelStatus = channelStatusService.readChannelStatusByRecipeId(recipe.getId());
		
		Long timestamp = 0L;
		if(channelStatus == null){
			timestamp = Calendar.getInstance().getTimeInMillis() - (rate);
			channelStatus = channelStatusService.createNewChannelStatus(recipe.getId(), timestamp/1000);
		}
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
		
		timestamp += rate;
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
		
		Long timestampMax = 0L;
		Long timestampMin = 0L;
		if(channelStatus == null){
			timestampMax = Calendar.getInstance().getTimeInMillis();
			timestampMin = Calendar.getInstance().getTimeInMillis() - (rate);
			channelStatus = channelStatusService.createNewChannelStatus(recipe.getId(), timestampMin, timestampMax);
		}
		else{
			timestampMin = channelStatus.getSinceRef();
			timestampMax = channelStatus.getFacebookSinceRef();
		}
		
		DateTime dateMax = new DateTime(timestampMax);
		DateTime dateMin = new DateTime(timestampMin);

		Events events = null;
		if(channelStatus.getPageToken() == null){
			events = calendar.events()
							.list("primary")
							.setMaxResults(50)
							.setOrderBy("startTime")
							.setSingleEvents(true)
							.setTimeMax(dateMax)
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
							.setTimeMax(dateMax)
							.setTimeMin(dateMin)
							.execute();
		}
		
		if(events.getNextPageToken() != null)
			channelStatus.setPageToken(events.getNextPageToken());
		
		timestampMax += rate;
		timestampMin += rate;
		channelStatus.setSinceRef(timestampMin);
		channelStatus.setFacebookSinceRef(timestampMax);
		
		channelStatusService.updateChannelStatus(channelStatus);
		
		List<Event> items = events.getItems();
        if (items.size() == 0){
            return items;
        }
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
	
	public String validateAndReplaceKeywords(String ingredient, int maxLength, Event event, Long triggerId){
		String ingredientReplaced = ingredient;
		Set<String> validKeywords = createRecipeService.readChannelKeywords(triggerId, "gcalendar");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		int index = 0;
		
		String title = event.getSummary() == null? "" : event.getSummary();
		String description = event.getDescription()== null? "" : event.getDescription();
		String location = event.getLocation()== null? "" : event.getLocation();
		Calendar startDate = Calendar.getInstance();
		Calendar endDate = Calendar.getInstance();
		if(event.getStart() != null && event.getStart().getDateTime() != null)
			startDate.setTimeInMillis(event.getStart().getDateTime().getValue());
		if(event.getEnd() != null && event.getEnd().getDateTime() != null)
			endDate.setTimeInMillis(event.getEnd().getDateTime().getValue());
		
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
					case "description" :
						ingredientReplaced = ingredientReplaced.replace("[" + keyword + "]", description);
						break;
					case "title" :
						ingredientReplaced = ingredientReplaced.replace("[" + keyword + "]", title);
						break;
					case "location" :
						ingredientReplaced = ingredientReplaced.replace("[" + keyword + "]", location);
						break;
					case "start_date" :
						ingredientReplaced = ingredientReplaced.replace("[" + keyword + "]", dateFormat.format(startDate.getTime()));
						break;
					case "start_time" :
						ingredientReplaced = ingredientReplaced.replace("[" + keyword + "]", timeFormat.format(startDate.getTime()));
						break;
					case "end_date" :
						ingredientReplaced = ingredientReplaced.replace("[" + keyword + "]", dateFormat.format(endDate.getTime()));
						break;
					case "end_time" :
						ingredientReplaced = ingredientReplaced.replace("[" + keyword + "]", timeFormat.format(endDate.getTime()));
						break;
				}
			}
		}

		if(ingredientReplaced.length() > maxLength && (maxLength - 4) > 0)
			ingredientReplaced = ingredientReplaced.substring(0, maxLength - 4) + "...";
		
		return ingredientReplaced;
	}
}
