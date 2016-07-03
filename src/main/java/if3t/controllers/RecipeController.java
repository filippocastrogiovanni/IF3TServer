package if3t.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import if3t.models.Recipe;
import if3t.services.RecipeService;

@RestController
@CrossOrigin
public class RecipeController {

	@Autowired
	private RecipeService recipeService;
	
	@RequestMapping(value="/recipes/{username}", method=RequestMethod.GET)
	public List<Recipe> getUserRecipes(@PathVariable String username) {
		return recipeService.readUserRecipes(username);
	}
	
	@RequestMapping(value="/public_recipes", method=RequestMethod.GET)
	public List<Recipe> getPublicRecipes() {
		return recipeService.readPublicRecipes();
	}
	
	@RequestMapping(value="/recipe/{id}", method=RequestMethod.GET)
	public List<Recipe> readRecipe(@PathVariable Long id) {
		return recipeService.readRecipe(id);
	}
	
	@RequestMapping(value="/add_recipe", method=RequestMethod.POST)
	public void addRecipe(@RequestBody Recipe recipe) {
		//TODO check if the user has authorization for the channels of the recipe
		recipeService.addRecipe(recipe);;
	}
	
	@RequestMapping(value="/remove_recipe/{id}", method=RequestMethod.POST)
	public void delRecipe(@PathVariable Long id) {
		recipeService.deleteRecipe(id);
	}
	
	@RequestMapping(value="/update_recipe", method=RequestMethod.PUT)
	public void updateRecipe(@RequestBody Recipe recipe) {
		recipeService.updateRecipe(recipe);
	}
}
