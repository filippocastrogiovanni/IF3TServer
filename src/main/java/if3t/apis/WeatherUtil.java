package if3t.apis;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import if3t.entities.ChannelStatus;
import if3t.services.ChannelStatusService;

@Component
public class WeatherUtil 
{
	@Autowired
	private ChannelStatusService channelStatusService;
	private static final String CONSUMER_KEY = "6a8e03e09652ab40025542b5fcd803e6";
	private static final String BASE_WEATHER_URL = "http://api.openweathermap.org/data/2.5/";
	private static final String URL_CURRENT_WEATHER = BASE_WEATHER_URL + "weather?appid=" + CONSUMER_KEY;
	private static final String URL_FORECAST_WEATHER = BASE_WEATHER_URL + "forecast?appid=" + CONSUMER_KEY; 
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
	
	public enum UnitsFormat { CELSIUS, KELVIN, FAHRENHEIT }
	
	private HttpResponse executeRequest(String url) throws IOException
	{
		HttpTransport transport = new NetHttpTransport();
		HttpRequestFactory factory = transport.createRequestFactory();
    	HttpRequest request = factory.buildGetRequest(new GenericUrl(url));
    	return request.execute();
	}
	
	private OffsetDateTime normalizeDate(Long epochSeconds, ZoneOffset offset)
	{
		System.out.println(epochSeconds);
		Instant instant = Instant.ofEpochSecond(epochSeconds);
		return OffsetDateTime.ofInstant(instant, offset);
	}
	
	//TODO in un task da eseguire ogni 15 minuti, recupero le ricette con questo tipo di trigger e chiamo questa funzione.
	//Scarico il JSON relativo alla località e controllo l'ora dell'alba. Se (normalizzata) è al massimo 15 minuti dall'ora attuale
	//allora scatta l'evento e nel DB salvo l'ultima data alla quale è scattato (se serve)
	public boolean isSunriseEventTriggered(Long cityId, Long recipeId)
	{
		try 
		{
			HttpResponse response = executeRequest(URL_CURRENT_WEATHER + "&id=" + cityId);
			ChannelStatus weatherStatus = channelStatusService.readChannelStatusByRecipeId(recipeId);
			
			if (response.getStatusCode() == 200)
			{
				System.out.println("xxx1");
				JSONObject respObject = new JSONObject(response.parseAsString());
				
				if (!respObject.has("cod") || respObject.isNull("cod"))
				{
					response.disconnect();
					return false;
				}
				
				if (respObject.getInt("cod") != 200)
				{
					response.disconnect();
					logger.error((respObject.has("message") && !respObject.isNull("message")) ? respObject.getInt("cod") + " - " + respObject.getString("message") : "Error: maybe the passed city ID was incorrect");
					return false;
				}
				
				if (!respObject.has("dt") || respObject.isNull("dt"))
				{
					response.disconnect();
					return false;
				}
				
				// If received JSON file is older then the last read one, than do not continue
				if (weatherStatus != null && respObject.getLong("dt") <= weatherStatus.getSinceRef())
				{
					response.disconnect();
					return false;
				}
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				if (respObject.has("cod") && respObject.getInt("cod") != 200)
				{
					System.out.println("xxx2");
					response.disconnect();
					logger.error((respObject.has("message") && !respObject.isNull("message")) ? respObject.getInt("cod") + " - " + respObject.getString("message") : "Error: maybe the passed city ID was incorrect");
					return false;
				}
				
				// If received JSON file is older then the last read one, than do not continue
				if (weatherStatus != null && respObject.has("dt") && respObject.getLong("dt") <= weatherStatus.getSinceRef())
				{
					System.out.println("xxx3");
					response.disconnect();
					return false;
				}
				
				if (weatherStatus == null && respObject.has("dt")) {
					System.out.println("xxx4");
            		weatherStatus = channelStatusService.createNewChannelStatus(recipeId, respObject.getLong("dt"));
            	}
            	else if (weatherStatus != null && respObject.has("dt"))
            	{
            		System.out.println("xxx5");
            		weatherStatus.setSinceRef(respObject.getLong("dt"));
            		weatherStatus = channelStatusService.updateChannelStatus(weatherStatus);
            	}
				
				if (!respObject.has("sys") || respObject.isNull("sys"))
				{
					System.out.println("xxx6");
					response.disconnect();
					return false;
				}
				
				JSONObject sysObject = respObject.getJSONObject("sys");
				
				if (!sysObject.has("sunrise") || sysObject.isNull("sunrise"))
				{
					System.out.println("xxx7");
					response.disconnect();
					return false;
				}
								
				OffsetDateTime now = OffsetDateTime.now().withNano(0);
				OffsetDateTime sunrise = normalizeDate(sysObject.getLong("sunrise"), now.getOffset());
				
				System.out.println(now + " - " + sunrise);
				
				if (sunrise.isBefore(now))
				{
					System.out.println("xxx8");
					response.disconnect();
					return false;
				}
				
				// WARNING: the field facebookSinceRef is used for store the epoch of the last triggered sunrise
				if (weatherStatus.getFacebookSinceRef() != null)
				{
					OffsetDateTime lastSunrise = normalizeDate(weatherStatus.getFacebookSinceRef(), now.getOffset());
					Duration difference = Duration.between(lastSunrise, sunrise).abs();
					
					System.out.println(difference + " - " + difference.toHours());
					
					if (difference.toMinutes() < 60)
					{
						System.out.println("xxx9");
						response.disconnect();
						return false;
					}
				}
				
				System.out.println(Duration.between(now, sunrise).toMinutes());
				
				if (Duration.between(sunrise, now).toMinutes() > 15)
				{
					System.out.println("xxx10");
					response.disconnect();
					return false;
				}
				
				// WARNING: the field facebookSinceRef is used for store the epoch of the last triggered sunrise
				weatherStatus.setFacebookSinceRef(sysObject.getLong("sunrise"));
				channelStatusService.updateChannelStatus(weatherStatus);
				//FIXME sostituire l'id col nome della città se possibile
				logger.info("The sun in " + cityId + " will rise within 15 minutes");
				response.disconnect();
				return true;
			}
			else
			{
				response.disconnect();
				logger.error("A problem occurred during the communication with the weather web service");
				return false;
			}
		}
		catch (JSONException e)
		{
			logger.error("A problem occurred during the parsing of the JSON response", e);
			return false;
		}
		catch (IOException e) 
		{
			logger.error("Failed to communicate with the weather web service", e);
			return false;
		}    	
	}
}
