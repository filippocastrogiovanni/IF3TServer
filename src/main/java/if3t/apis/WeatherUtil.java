package if3t.apis;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
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
import if3t.entities.TriggerIngredient;
import if3t.services.ChannelStatusService;
import if3t.services.CreateRecipeService;

@Component
public class WeatherUtil 
{
	@Autowired
	private ChannelStatusService channelStatusService;
	@Autowired
	private CreateRecipeService createRecipeService;
	private static final String CONSUMER_KEY = "6a8e03e09652ab40025542b5fcd803e6";
	private static final String BASE_WEATHER_URL = "http://api.openweathermap.org/data/2.5/";
	private static final String URL_CURRENT_WEATHER = BASE_WEATHER_URL + "weather?appid=" + CONSUMER_KEY;
	private static final String URL_FORECAST_WEATHER = BASE_WEATHER_URL + "forecast?appid=" + CONSUMER_KEY; 
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
	
	public enum SunriseSunsetMode { SUNRISE, SUNSET }
	public enum TempAboveBelowMode { ABOVE, BELOW }
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
		Instant instant = Instant.ofEpochSecond(epochSeconds);
		return OffsetDateTime.ofInstant(instant, offset);
	}
	
	//TODO eliminare alla fine i commenti
	public String getReportOnSunriseOrSunset(Long cityId, Long recipeId, SunriseSunsetMode eventType, UnitsFormat format)
	{
		String eventName = eventType.toString().toLowerCase();
		String finalUrl = URL_CURRENT_WEATHER + "&id=" + cityId;
		
		if (format != UnitsFormat.KELVIN) {
			finalUrl += "&units=" + ((format == UnitsFormat.CELSIUS) ? "metric" : "imperial");
		}
		
		try 
		{	
			HttpResponse response = executeRequest(finalUrl);
			
			if (response.getStatusCode() == 200)
			{
//				System.out.println("xxx1");
				JSONObject respObject = new JSONObject(response.parseAsString());
				
				if (!respObject.has("cod") || respObject.isNull("cod"))
				{
//					System.out.println("xxx2");
					response.disconnect();
					return null;
				}
				
				if (respObject.getInt("cod") != 200)
				{
//					System.out.println("xxx3");
					response.disconnect();
					logger.error((respObject.has("message") && !respObject.isNull("message")) ? respObject.getInt("cod") + " - " + respObject.getString("message") : "Error: maybe the passed city id was incorrect");
					return null;
				}
				
				if (!respObject.has("dt") || respObject.isNull("dt"))
				{
//					System.out.println("xxx4");
					response.disconnect();
					return null;
				}
				
				ChannelStatus weatherStatus = channelStatusService.readChannelStatusByRecipeId(recipeId);
				
				// If received JSON file is older (or equal) then the last read one, than do not continue
				if (weatherStatus != null && respObject.getLong("dt") <= weatherStatus.getSinceRef())
				{
//					System.out.println("xxx5");
					response.disconnect();
					return null;
				}
				
				if (!respObject.has("sys") || respObject.isNull("sys"))
				{
//					System.out.println("xxx6");
					response.disconnect();
					return null;
				}
				
				JSONObject sysObject = respObject.getJSONObject("sys");
				
				if (!sysObject.has(eventName) || sysObject.isNull(eventName))
				{
//					System.out.println("xxx7");
					response.disconnect();
					return null;
				}
								
				OffsetDateTime now = OffsetDateTime.now().withNano(0);
				OffsetDateTime eventOdt = normalizeDate(sysObject.getLong(eventName), now.getOffset());
				
				if (eventOdt.isBefore(now))
				{
//					System.out.println("xxx8");
					response.disconnect();
					return null;
				}
				
				if (weatherStatus != null)
				{
//					System.out.println("xxx9");
					// WARNING: the field facebookSinceRef is used for store the epoch of the last triggered sunrise/sunset
					OffsetDateTime lastEventOdt = normalizeDate(weatherStatus.getFacebookSinceRef(), now.getOffset());
					
					// In a new JSON file (dt > of the old one) the sunrise/sunset it's the same but the relative time is lightly different
					if (Duration.between(lastEventOdt, eventOdt).abs().toMinutes() < 60)
					{
//						System.out.println("xxx10");
						response.disconnect();
						return null;
					}
				}
				
				if (Duration.between(now, eventOdt).toMinutes() > 15)
				{
//					System.out.println("xxx11");
					response.disconnect();
					return null;
				}
				
				if (weatherStatus == null) 
				{
//					System.out.println("xxx12");
					// WARNING: the field facebookSinceRef is used for store the epoch of the last triggered sunrise/sunset
            		channelStatusService.createNewChannelStatus(recipeId, respObject.getLong("dt"), sysObject.getLong(eventName));
            	}
            	else
            	{
//            		System.out.println("xxx13");
            		// WARNING: the field facebookSinceRef is used for store the epoch of the last triggered sunrise/sunset
            		weatherStatus.setFacebookSinceRef(sysObject.getLong(eventName));
            		weatherStatus.setSinceRef(respObject.getLong("dt"));
            		channelStatusService.updateChannelStatus(weatherStatus);
            	}
				
				response.disconnect();
				String city = (respObject.has("name") && !respObject.isNull("name")) ? respObject.getString("name") : "the city the id of which is " + cityId;
				logger.info("The sun in " + city + " will " + eventName.substring(3) + " within 15 minutes");
				
				StringBuffer returnMsg = new StringBuffer();
				returnMsg.append(((returnMsg.length() > 0) ? " It's " : "It's ") + eventName + ".");
				
				if (respObject.has("main") && !respObject.isNull("main"))
				{
					JSONObject mainObject = respObject.getJSONObject("main");
					
					if (mainObject.has("temp") && !mainObject.isNull("temp"))
					{
						returnMsg.append(" The temperature at the moment is ");
						returnMsg.append(mainObject.getDouble("temp") + format.toString().substring(0, 1) + ".");
					}
				}
				
				if (respObject.has("weather") && !respObject.isNull("weather"))
				{
					JSONArray weatherArray = respObject.getJSONArray("weather");
					
					if (weatherArray.length() > 0)
					{
						JSONObject weatherObject = (JSONObject) weatherArray.get(0);
						
						if (weatherObject.has("description") && !weatherObject.isNull("description"))
						{
							returnMsg.append(" Current conditions are '");
							returnMsg.append(weatherObject.getString("description") + "'.");
						}
					}
				}
				
				return returnMsg.toString();
			}
			else
			{
				response.disconnect();
				logger.error("A problem occurred during the communication with the weather web service");
				return null;
			}
		}
		catch (JSONException e)
		{
			logger.error("A problem occurred during the parsing of the JSON response", e);
			return null;
		}
		catch (IOException e) 
		{
			logger.error("Failed to communicate with the weather web service", e);
			return null;
		}    	
	}
	
	public String getEventTemperatureAboveOrBelow(Long cityId, Long recipeId, TempAboveBelowMode eventType, double threshold, UnitsFormat format)
	{
		String finalUrl = URL_CURRENT_WEATHER + "&id=" + cityId;
		
		if (format != UnitsFormat.KELVIN) {
			finalUrl += "&units=" + ((format == UnitsFormat.CELSIUS) ? "metric" : "imperial");
		}
		
		try 
		{	
			HttpResponse response = executeRequest(finalUrl);
			
			if (response.getStatusCode() == 200)
			{
//				System.out.println("xxx1");
				JSONObject respObject = new JSONObject(response.parseAsString());
				
				if (!respObject.has("cod") || respObject.isNull("cod"))
				{
//					System.out.println("xxx2");
					response.disconnect();
					return null;
				}
				
				if (respObject.getInt("cod") != 200)
				{
//					System.out.println("xxx3");
					response.disconnect();
					logger.error((respObject.has("message") && !respObject.isNull("message")) ? respObject.getInt("cod") + " - " + respObject.getString("message") : "Error: maybe the passed city id was incorrect");
					return null;
				}
				
				if (!respObject.has("dt") || respObject.isNull("dt"))
				{
//					System.out.println("xxx4");
					response.disconnect();
					return null;
				}
				
				ChannelStatus weatherStatus = channelStatusService.readChannelStatusByRecipeId(recipeId);
				
				// If received JSON file is older (or equal) then the last read one, than do not continue
				if (weatherStatus != null && respObject.getLong("dt") <= weatherStatus.getSinceRef())
				{
//					System.out.println("xxx5");
					response.disconnect();
					return null;
				}
				
				if (!respObject.has("main") || respObject.isNull("main"))
				{
//					System.out.println("xxx6");
					response.disconnect();
					return null;
				}
					
				JSONObject mainObject = respObject.getJSONObject("main");
					
				if (!mainObject.has("temp") || mainObject.isNull("temp"))
				{
//					System.out.println("xxx7");
					response.disconnect();
					return null;
				}
			
				String message;
				String city = (respObject.has("name") && !respObject.isNull("name")) ? respObject.getString("name") : "the city the id of which is " + cityId;
				
				if (weatherStatus == null) 
				{
					// WARNING: the field pageToken is used for store the last read temperature (double)
					channelStatusService.createNewChannelStatus(recipeId, respObject.getLong("dt"), String.valueOf(mainObject.getDouble("temp")));
					
					if (eventType == TempAboveBelowMode.ABOVE)
					{
//						System.out.println("xxx8");
						if (mainObject.getDouble("temp") > threshold)
						{
//							System.out.println("xxx9");
							response.disconnect();
							message = "The temperature in " + city + " has risen above the threshold of " + threshold + format.toString().substring(0, 1);
							logger.info(message);
							return message;
						}
					}
					else
					{
//						System.out.println("xxx10");
						if (mainObject.getDouble("temp") < threshold)
						{
//							System.out.println("xxx11");
							response.disconnect();
							message = "The temperature in " + city + " has dropped below the threshold of " + threshold + format.toString().substring(0, 1);
							logger.info(message);
							return message;
						}
					}
					
					response.disconnect();
					return null;
				}
				
				double lastTemp = Double.parseDouble(weatherStatus.getPageToken());
				weatherStatus.setSinceRef(respObject.getLong("dt"));
				// WARNING: the field pageToken is used for store the last read temperature (double)
				weatherStatus.setPageToken(String.valueOf(mainObject.getDouble("temp")));
				channelStatusService.updateChannelStatus(weatherStatus);
					
				if (eventType == TempAboveBelowMode.ABOVE)
				{
//					System.out.println("xxx12");
					if (mainObject.getDouble("temp") > threshold && lastTemp <= threshold)
					{
//						System.out.println("xxx13");
						response.disconnect();
						message = "The temperature in " + city + " has risen above the threshold of " + threshold + format.toString().substring(0, 1);
						logger.info(message);
						return message;
					}
				}
				else
				{
//					System.out.println("xxx14");
					if (mainObject.getDouble("temp") < threshold && lastTemp >= threshold)
					{
//						System.out.println("xxx15");
						response.disconnect();
						message = "The temperature in " + city + " has dropped below the threshold of " + threshold + format.toString().substring(0, 1);
						logger.info(message);
						return message;
					}
				}
					
				response.disconnect();
				return null;
			}
			else
			{
				response.disconnect();
				logger.error("A problem occurred during the communication with the weather web service");
				return null;
			}
		}
		catch (JSONException e)
		{
			logger.error("A problem occurred during the parsing of the JSON response", e);
			return null;
		}
		catch (IOException e) 
		{
			logger.error("Failed to communicate with the weather web service", e);
			return null;
		}    	
	}
	
	public String getTomorrowReport(Long cityId, Long recipeId, String time, String zoneId, UnitsFormat format)
	{
		ChannelStatus weatherStatus = channelStatusService.readChannelStatusByRecipeId(recipeId);
		
		if (weatherStatus != null)
		{
			// WARNING: the field sinceRef is used to store the last time this trigger has fired
			if (Duration.between(Instant.now(), Instant.ofEpochSecond(weatherStatus.getSinceRef())).abs().toHours() < 23) {
				return null;
			}
		}
		
		OffsetTime serverNowTime = OffsetTime.now().withNano(0);
		LocalTime trigLocalTime = LocalTime.parse(time);
		OffsetTime userNowTime = OffsetTime.now(ZoneId.of(zoneId)).withNano(0);
		OffsetTime trigOffsetTime = OffsetTime.of(trigLocalTime, userNowTime.getOffset());
		OffsetTime normTrigOffsetTime = trigOffsetTime.withOffsetSameInstant(serverNowTime.getOffset());
		
		if (Duration.between(serverNowTime, normTrigOffsetTime).abs().toMinutes() > 5) {
			return null;
		}		
		
		String finalUrl = URL_FORECAST_WEATHER + "&id=" + cityId;
		
		if (format != UnitsFormat.KELVIN) {
			finalUrl += "&units=" + ((format == UnitsFormat.CELSIUS) ? "metric" : "imperial");
		}
		
		try 
		{	
			HttpResponse response = executeRequest(finalUrl);
			
			if (response.getStatusCode() == 200)
			{
//				System.out.println("xxx1");
				JSONObject respObject = new JSONObject(response.parseAsString());
				
				if (!respObject.has("cod") || respObject.isNull("cod"))
				{
//					System.out.println("xxx2");
					response.disconnect();
					return null;
				}
				
				if (respObject.getInt("cod") != 200)
				{
//					System.out.println("xxx3");
					response.disconnect();
					logger.error((respObject.has("message") && !respObject.isNull("message")) ? respObject.getInt("cod") + " - " + respObject.getString("message") : "Error: maybe the passed city id was incorrect");
					return null;
				}
				
				if (!respObject.has("list") || respObject.isNull("list"))
				{
//					System.out.println("xxx4");
					response.disconnect();
					return null;
				}
				
				OffsetDateTime firstDate = OffsetDateTime.MAX;
				double maxTemp = Double.MIN_VALUE;
				double minTemp = Double.MAX_VALUE;
				HashMap<String, Integer> weatherStats = new HashMap<String, Integer>();
				
				JSONArray forecastArray = respObject.getJSONArray("list");
				
				for (int i = 0; i < forecastArray.length(); i++)
				{
					JSONObject fcObject = (JSONObject) forecastArray.get(i);
					
					if (!fcObject.has("dt") || fcObject.isNull("dt"))
					{
//						System.out.println("xxx5");
						response.disconnect();
						return null;
					}
					
					// The current day in the specified city is supposed to be in the first object
					if (i == 0)
					{
						firstDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(fcObject.getLong("dt")), ZoneId.of("Etc/GMT"));
						continue;
					}
					
					OffsetDateTime currentDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(fcObject.getLong("dt")), ZoneId.of("Etc/GMT"));
					
					if (currentDate.getDayOfYear() != firstDate.plusDays(1).getDayOfYear())	{
						continue;
					}
					
					if (!fcObject.has("main") || fcObject.isNull("main"))
					{
//						System.out.println("xxx6");
						response.disconnect();
						return null;
					}
					
					JSONObject mainObject = fcObject.getJSONObject("main");
					
					if (!mainObject.has("temp_min") || mainObject.isNull("temp_min") || !mainObject.has("temp_max") || mainObject.isNull("temp_max"))
					{
//						System.out.println("xxx7");
						response.disconnect();
						return null;
					}
					
					if (!fcObject.has("weather") || fcObject.isNull("weather"))
					{
//						System.out.println("xxx8");
						response.disconnect();
						return null;
					}
					
					JSONArray weatherArray = fcObject.getJSONArray("weather");
					
					if (weatherArray.length() == 0)
					{
//						System.out.println("xxx9");
						response.disconnect();
						return null;
					}
						
					JSONObject weatherObject = (JSONObject) weatherArray.get(0);
					
					if (!weatherObject.has("main") || weatherObject.isNull("main"))
					{
//						System.out.println("xxx10");
						response.disconnect();
						return null;
					}
					
					if (mainObject.getDouble("temp_min") < minTemp) {
						minTemp = mainObject.getDouble("temp_min");
					}
					
					if (mainObject.getDouble("temp_max") > maxTemp) {
						maxTemp = mainObject.getDouble("temp_max");
					}
					
					if (!weatherStats.containsKey(weatherObject.getString("main"))) {
						weatherStats.put(weatherObject.getString("main"), 1);
					}
					else {
						weatherStats.put(weatherObject.getString("main"), weatherStats.get(weatherObject.getString("main")) + 1);
					}
				}
				
				// WARNING: the field sinceRef is used to store the last time this trigger has fired
				if (weatherStatus == null) {
					channelStatusService.createNewChannelStatus(recipeId, Instant.now().getEpochSecond());
				}
				else 
				{
					weatherStatus.setSinceRef(Instant.now().getEpochSecond());
					channelStatusService.updateChannelStatus(weatherStatus);
				}
				
				String cityName = "";
				StringBuffer sb = new StringBuffer("");
				
				if (respObject.has("city") && !respObject.isNull("city"))
				{
					JSONObject cityObject = respObject.getJSONObject("city");
					
					if (cityObject.has("name") && !cityObject.isNull("name")) {
						cityName = cityObject.getString("name");
					}
				}
				
				if (weatherStats.size() > 0)
				{
					TreeMap<Integer, String> sortedWeatherStats = new TreeMap<Integer, String>();
					
					for (String key : weatherStats.keySet())
					{
						sortedWeatherStats.put(weatherStats.get(key), key);
					}
					
					if (sortedWeatherStats.size() > 1) {
						sb.append("Mostly ");
					}
					
					sb.append(sortedWeatherStats.lastEntry().getValue());
					sb.append(" tomorrow");
					sb.append((cityName.length() > 0 ? " in " + cityName : "") + "!");
				}
				
				if (minTemp != Double.MAX_VALUE && maxTemp != Double.MIN_VALUE)
				{
					sb.append("\nWith a high of " + maxTemp + format.toString().substring(0, 1));
					sb.append(" and a low of " + minTemp + format.toString().substring(0, 1) + ".");
				}
				
				response.disconnect();
				logger.info((cityName.length() > 0 ? cityName : "City the id of which is " + cityId) + ": " + sb.toString().substring(0, sb.indexOf(" tomorrow") + 9) + "!");
				return sb.toString(); 
			}
			else
			{
				response.disconnect();
				logger.error("A problem occurred during the communication with the weather web service");
				return null;
			}
		}
		catch (JSONException e)
		{
			logger.error("A problem occurred during the parsing of the JSON response", e);
			return null;
		}
		catch (IOException e) 
		{
			logger.error("Failed to communicate with the weather web service", e);
			return null;
		}   
	}
	
	public String replaceKeywords(String text, List<TriggerIngredient> trigIngrList, int maxLength)
	{
		String target;
		Set<String> validKeywords = createRecipeService.readChannelKeywords("weather");
		
		for (String vk : validKeywords)
		{
			target = "[" + vk + "]";
			
			for (TriggerIngredient ti: trigIngrList)
			{
				if (ti.getParam().getKeyword().equals(vk))
				{
					text = text.replace(target, ti.getValue());
					break;
				}
			}
		}
		
		return (maxLength < 0 || text.length() <= maxLength) ? text : text.substring(0, maxLength - 4).concat(" ...");
	}
}