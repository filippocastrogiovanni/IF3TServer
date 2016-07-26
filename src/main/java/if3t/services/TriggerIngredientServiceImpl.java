package if3t.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.models.TriggerIngredient;
import if3t.repositories.TriggerIngredientRepository;

@Service
@Transactional
public class TriggerIngredientServiceImpl implements TriggerIngredientService
{
	@Autowired
	private TriggerIngredientRepository triggerIngredientRepo;
	
	@Override
	public TriggerIngredient readTriggerIngredient(Long id) {
		return triggerIngredientRepo.findOne(id);
	}

	@Override
	public List<TriggerIngredient> readTriggerIngredients() 
	{
		List<TriggerIngredient> list = new ArrayList<TriggerIngredient>();
		
		for (TriggerIngredient t: triggerIngredientRepo.findAll()) 
		{
			list.add(t);
		}
		
		return list;
	}

	@Override
	public List<TriggerIngredient> getRecipeTriggerIngredients(Long recipeId) {
		return triggerIngredientRepo.findByRecipe_Id(recipeId);
	}

	@Override
	public Map<Long, TriggerIngredient> getRecipeTriggerIngredientsMap(Long recipeId) 
	{
		Map<Long, TriggerIngredient> map = new HashMap<Long, TriggerIngredient>();
		
		for (TriggerIngredient ti : triggerIngredientRepo.findByRecipe_Id(recipeId))
		{
			map.putIfAbsent(ti.getParam().getId(), ti);
		}
		
		return map;
	}
}