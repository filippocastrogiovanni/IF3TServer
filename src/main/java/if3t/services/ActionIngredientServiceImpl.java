package if3t.services;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.models.ActionIngredient;
import if3t.models.TriggerIngredient;
import if3t.repositories.ActionIngredientRepository;
import if3t.repositories.TriggerIngredientRepository;

@Service
@Transactional
public class ActionIngredientServiceImpl implements ActionIngredientService{

	@Autowired
	private ActionIngredientRepository actionIngredientRepo;
	
	public ActionIngredient readActionIngredient(Long id) {
		return actionIngredientRepo.findOne(id);
	}

	public List<ActionIngredient> readActionIngredients() {
		ArrayList<ActionIngredient> list = new ArrayList<ActionIngredient>();
		for(ActionIngredient t: actionIngredientRepo.findAll())
			list.add(t);
		
		return list;
	}

	public List<ActionIngredient> getRecipeActionIngredients(Long recipeId) {
		return actionIngredientRepo.findByRecipe_Id(recipeId);
	}

}
