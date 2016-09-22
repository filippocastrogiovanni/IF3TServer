package if3t.services;

import java.util.List;

import if3t.entities.City;

public interface CityService 
{
	public City getCityById(Long id);
	public List<City> getCitiesWithPartOfName(String keyword, int limit);
}
