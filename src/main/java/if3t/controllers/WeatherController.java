package if3t.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import if3t.entities.Authorization;
import if3t.entities.City;
import if3t.entities.User;
import if3t.exceptions.NotLoggedInException;
import if3t.models.Response;
import if3t.services.AuthorizationService;
import if3t.services.ChannelService;
import if3t.services.CityService;
import if3t.services.UserService;

@CrossOrigin
@RestController
public class WeatherController 
{
	@Autowired
	private UserService userService;
	@Autowired
	private ChannelService channelService;
	@Autowired
	private AuthorizationService authorizationService;
	@Autowired
	private CityService cityService;
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/weather/stored_location", method = RequestMethod.GET)
	public Response getWeatherLocation() throws NotLoggedInException
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not logged in!");
		}
		
		User loggedUser = userService.getUserByUsername(auth.getName());
		Authorization storedAuth = authorizationService.getAuthorization(loggedUser.getId(), "weather");
		
		if (storedAuth == null) {
			return new Response(null, HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase());
		}
		
		City city = cityService.getCityById(Long.parseLong(storedAuth.getAccessToken()));
		String response = city.getId() + "&" + city.getName() + "&" + city.getCountry();
		
		// WARNING: access token for the channel weather doesn't exist, so the field is used to store the id of the location associated with it
		return new Response(response, HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/weather/query_locations/{keyword}", method = RequestMethod.GET)
	public List<City> getResponseWeatherQuery(@PathVariable String keyword) throws NotLoggedInException
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not logged in!");
		}
		
		return cityService.getCitiesWithPartOfName(keyword, 50);
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/weather/update_location/{id}", method = RequestMethod.PUT)
	public Response updateWeatherLocation(@PathVariable Long id) throws NotLoggedInException
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not logged in!");
		}
		
		if (cityService.getCityById(id) == null) {
			return new Response("There is no city the id of which is the passed one", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		
		User loggedUser = userService.getUserByUsername(auth.getName());
		// WARNING: access token for the channel weather doesn't exist, so the field is used to store the id of the location associated with it
		channelService.authorizeChannel(loggedUser.getId(), "weather", id.toString(), null, null, null);
		return new Response("The weather location has been created/updated successfully", HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
	}
}