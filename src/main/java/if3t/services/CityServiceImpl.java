package if3t.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
	public List<City> getCitiesWithPartOfName(String keyword, int limit) {
		return cityRepository.findCitiesWithPartOfName(keyword, new PageRequest(0, limit));
	}
}