package if3t.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.models.Action;
import if3t.repositories.ActionRepository;

@Service
@Transactional
public class ActionServiceImpl implements ActionService{
	@Autowired
	private ActionRepository actionRepository;
	
	public Action findById(Long id) {
		return actionRepository.findOne(id);
	}

}
