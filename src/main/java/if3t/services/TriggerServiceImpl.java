package if3t.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.models.Trigger;
import if3t.repositories.TriggerRepository;

@Service
@Transactional
public class TriggerServiceImpl implements TriggerService{
	@Autowired
	private TriggerRepository TriggerRepository;
	
	public Trigger findById(Long id) {
		return TriggerRepository.findOne(id);
	}

}