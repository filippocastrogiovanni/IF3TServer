package if3t.apis;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
	public boolean isSunriseOrSunset(Long cityId, Long recipeId, SunriseSunsetMode eventType, UnitsFormat format, StringBuffer returnMsg)
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
					return false;
				}
				
				if (respObject.getInt("cod") != 200)
				{
//					System.out.println("xxx3");
					response.disconnect();
					logger.error((respObject.has("message") && !respObject.isNull("message")) ? respObject.getInt("cod") + " - " + respObject.getString("message") : "Error: maybe the passed city id was incorrect");
					return false;
				}
				
				if (!respObject.has("dt") || respObject.isNull("dt"))
				{
//					System.out.println("xxx4");
					response.disconnect();
					return false;
				}
				
				ChannelStatus weatherStatus = channelStatusService.readChannelStatusByRecipeId(recipeId);
				
				// If received JSON file is older (or equal) then the last read one, than do not continue
				if (weatherStatus != null && respObject.getLong("dt") <= weatherStatus.getSinceRef())
				{
//					System.out.println("xxx5");
					response.disconnect();
					return false;
				}
				
				if (!respObject.has("sys") || respObject.isNull("sys"))
				{
//					System.out.println("xxx6");
					response.disconnect();
					return false;
				}
				
				JSONObject sysObject = respObject.getJSONObject("sys");
				
				if (!sysObject.has(eventName) || sysObject.isNull(eventName))
				{
//					System.out.println("xxx7");
					response.disconnect();
					return false;
				}
								
				OffsetDateTime now = OffsetDateTime.now().withNano(0);
				OffsetDateTime eventOdt = normalizeDate(sysObject.getLong(eventName), now.getOffset());
				
				if (eventOdt.isBefore(now))
				{
//					System.out.println("xxx8");
					response.disconnect();
					return false;
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
						return false;
					}
				}
				
				if (Duration.between(now, eventOdt).toMinutes() > 15)
				{
//					System.out.println("xxx11");
					response.disconnect();
					return false;
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
				
				//FIXME eliminare alla fine
				System.out.println(returnMsg);
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
	
	public boolean isTemperatureAboveOrBelow(Long cityId, Long recipeId, TempAboveBelowMode eventType, double threshold, UnitsFormat format)
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
					return false;
				}
				
				if (respObject.getInt("cod") != 200)
				{
//					System.out.println("xxx3");
					response.disconnect();
					logger.error((respObject.has("message") && !respObject.isNull("message")) ? respObject.getInt("cod") + " - " + respObject.getString("message") : "Error: maybe the passed city id was incorrect");
					return false;
				}
				
				if (!respObject.has("dt") || respObject.isNull("dt"))
				{
//					System.out.println("xxx4");
					response.disconnect();
					return false;
				}
				
				ChannelStatus weatherStatus = channelStatusService.readChannelStatusByRecipeId(recipeId);
				
				// If received JSON file is older (or equal) then the last read one, than do not continue
				if (weatherStatus != null && respObject.getLong("dt") <= weatherStatus.getSinceRef())
				{
//					System.out.println("xxx5");
					response.disconnect();
					return false;
				}
				
				if (!respObject.has("main") || respObject.isNull("main"))
				{
//					System.out.println("xxx6");
					response.disconnect();
					return false;
				}
					
				JSONObject mainObject = respObject.getJSONObject("main");
					
				if (!mainObject.has("temp") || mainObject.isNull("temp"))
				{
//					System.out.println("xxx7");
					response.disconnect();
					return false;
				}
			
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
							logger.info("The temperature in " + city + " has risen above the threshold of " + threshold + format.toString().substring(0, 1));
							return true;
						}
					}
					else
					{
//						System.out.println("xxx10");
						if (mainObject.getDouble("temp") < threshold)
						{
//							System.out.println("xxx11");
							response.disconnect();
							logger.info("The temperature in " + city + " has dropped below the threshold of " + threshold + format.toString().substring(0, 1));
							return true;
						}
					}
					
					response.disconnect();
					return false;
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
						logger.info("The temperature in " + city + " has risen above the threshold of " + threshold + format.toString().substring(0, 1));
						return true;
					}
				}
				else
				{
//					System.out.println("xxx14");
					if (mainObject.getDouble("temp") < threshold && lastTemp >= threshold)
					{
//						System.out.println("xxx15");
						response.disconnect();
						logger.info("The temperature in " + city + " has dropped below the threshold of " + threshold + format.toString().substring(0, 1));
						return true;
					}
				}
					
				response.disconnect();
				return false;
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
	
	public String getTomorrowWeatherReport(Long cityId, Long recipeId, String time, UnitsFormat format)
	{
		String finalUrl = URL_FORECAST_WEATHER + "&id=" + cityId;
		
		if (format != UnitsFormat.KELVIN) {
			finalUrl += "&units=" + ((format == UnitsFormat.CELSIUS) ? "metric" : "imperial");
		}
		
		try 
		{	
			HttpResponse response = executeRequest(finalUrl);
			
			if (response.getStatusCode() == 200)
			{
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
}