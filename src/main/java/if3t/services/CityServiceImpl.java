package if3t.services;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.entities.City;
import if3t.repositories.CityRepository;

@Service
@Transactional
public class CityServiceImpl implements CityService 
{
	@Autowired
	private CityRepository cityRepository;
	
	@Override
	public City getCityById(Long id) {
		return cityRepository.findOne(id);
	}
	
	@Override
	public Set<City> getCitiesWithPartOfName(String keyword) {
		return new HashSet<City>(cityRepository.findCitiesWithPartOfName(keyword));
	}
}