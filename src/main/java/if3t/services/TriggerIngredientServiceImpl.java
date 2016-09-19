package if3t.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.entities.Recipe;
import if3t.entities.TriggerIngredient;
import if3t.repositories.RecipeRepository;
import if3t.repositories.TriggerIngredientRepository;

@Service
@Transactional
public class TriggerIngredientServiceImpl implements TriggerIngredientService
{
	@Autowired
	private RecipeRepository recipeRepository;
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
	public Map<Long, TriggerIngredient> getRecipeTriggerIngredientsMap(String groupId) 
	{
		Map<Long, TriggerIngredient> map = new HashMap<Long, TriggerIngredient>();
		
		for (Recipe rec : recipeRepository.findByGroupId(groupId))
		{
			for (TriggerIngredient ti : triggerIngredientRepo.findByRecipe_Id(rec.getId()))
			{
				map.putIfAbsent(ti.getParam().getId(), ti);
			}
		}
		
		return map;
	}
}