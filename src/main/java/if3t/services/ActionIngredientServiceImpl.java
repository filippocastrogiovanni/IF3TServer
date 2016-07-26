package if3t.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.models.ActionIngredient;
import if3t.repositories.ActionIngredientRepository;

@Service
@Transactional
public class ActionIngredientServiceImpl implements ActionIngredientService
{
	@Autowired
	private ActionIngredientRepository actionIngredientRepo;
	
	@Override
	public ActionIngredient readActionIngredient(Long id) {
		return actionIngredientRepo.findOne(id);
	}

	@Override
	public List<ActionIngredient> readActionIngredients() 
	{
		List<ActionIngredient> list = new ArrayList<ActionIngredient>();
		
		for (ActionIngredient t: actionIngredientRepo.findAll())
		{
			list.add(t);
		}
		
		return list;
	}

	@Override
	public List<ActionIngredient> getRecipeActionIngredients(Long recipeId) {
		return actionIngredientRepo.findByRecipe_Id(recipeId);
	}

	@Override
	public Map<Long, ActionIngredient> getRecipeActionIngredientsMap(Long recipeId) 
	{
		Map<Long, ActionIngredient> map = new HashMap<Long, ActionIngredient>();
		
		for (ActionIngredient ai : actionIngredientRepo.findByRecipe_Id(recipeId))
		{
			map.putIfAbsent(ai.getParam().getId(), ai);
		}
		
		return map;
	}
}