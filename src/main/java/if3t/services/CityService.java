package if3t.services;

import java.util.Set;

import if3t.entities.City;

public interface CityService 
{
	public Set<City> getCitiesWithPartOfName(String keyword);
}
