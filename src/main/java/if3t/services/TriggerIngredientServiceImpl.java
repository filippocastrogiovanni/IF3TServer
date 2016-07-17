package if3t.services;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.models.TriggerIngredient;
import if3t.repositories.TriggerIngredientRepository;

@Service
@Transactional
public class TriggerIngredientServiceImpl implements TriggerIngredientService{

	@Autowired
	private TriggerIngredientRepository triggerIngredientRepo;
	
	public TriggerIngredient readTriggerIngredient(Long id) {
		return triggerIngredientRepo.findOne(id);
	}

	public List<TriggerIngredient> readTriggerIngredients() {
		ArrayList<TriggerIngredient> list = new ArrayList<TriggerIngredient>();
		for(TriggerIngredient t: triggerIngredientRepo.findAll())
			list.add(t);
		
		return list;
	}

	public List<TriggerIngredient> getRecipeTriggerIngredients(Long recipeId) {
		return triggerIngredientRepo.findByRecipe_Id(recipeId);
	}

}
