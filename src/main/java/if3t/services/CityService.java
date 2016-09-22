package if3t.services;

import java.util.Set;

import if3t.entities.City;

public interface CityService 
{
	public City getCityById(Long id);
	public Set<City> getCitiesWithPartOfName(String keyword);
}
