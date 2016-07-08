package if3t.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import if3t.models.Recipe;
import if3t.repositories.RecipeRepository;

@Service
@Transactional
public class RecipeServiceImpl implements RecipeService {

	@Autowired
	private RecipeRepository recipeRepository;
	
	public List<Recipe> readUserRecipes(Long userId) {
		return recipeRepository.findByUser_Id(userId);
	}

	public List<Recipe> readPublicRecipes() {
		return recipeRepository.findByIsPublic(true);
	}

	public List<Recipe> readRecipe(Long id) {
		Recipe recipe = recipeRepository.findOne(id);
		String groupId = recipe.getGroupId();
		return recipeRepository.findByGroupId(groupId);
	}

	public void deleteRecipe(Long id) {
		Recipe recipe = recipeRepository.findOne(id);
		recipeRepository.delete(recipe);
	}

	public void addRecipe(List<Recipe> recipe) {
		UUID groupId = UUID.randomUUID();
		for(Recipe r: recipe){
			r.setGroupId(groupId.toString());
			recipeRepository.save(r);
		}
	}

	public void updateRecipe(Recipe recipe) {
		recipeRepository.save(recipe);
	}

}
