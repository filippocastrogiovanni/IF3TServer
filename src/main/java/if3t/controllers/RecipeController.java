package if3t.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.NotLoggedInException;
import if3t.models.Recipe;
import if3t.models.Response;
import if3t.models.User;
import if3t.services.RecipeService;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class RecipeController {

	@Autowired
	private RecipeService recipeService;
	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/user_recipes", method=RequestMethod.GET)
	public List<Recipe> getUserRecipes() throws NotLoggedInException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth == null)
			throw new NotLoggedInException("ERROR: not loggedIn");
		
		user = userService.getUserByUsername(auth.getName());
		return recipeService.readUserRecipes(user.getId());
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
	public void addRecipe(@RequestBody List<Recipe> recipe) throws NotLoggedInException {
		//TODO controlli user
		System.out.println("ADDING RECIPE");
		recipeService.addRecipe(recipe);
	}
	
	@RequestMapping(value="/remove_recipe/{id}", method=RequestMethod.POST)
	public void delRecipe(@PathVariable Long id) {
		//TODO controlli user
		recipeService.deleteRecipe(id);
	}
	
	@RequestMapping(value="/publish_recipe", method=RequestMethod.PUT)
	public void publishRecipe(@RequestBody Recipe recipe) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth != null)
			user = userService.getUserByUsername(auth.getName());
		recipe.setUser(user);
		recipe.setIsPublic(recipe.getIsPublic());
		recipeService.publishRecipe(recipe);
	}
	
	@RequestMapping(value="/enable_recipe", method=RequestMethod.PUT)
	public Response enableRecipe(@RequestBody Recipe recipe) throws NotLoggedInException, ChannelNotAuthorizedException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth == null)
			throw new NotLoggedInException("ERROR: not loggedIn");
		
		user = userService.getUserByUsername(auth.getName());
		recipe.setUser(user);
		recipeService.enableRecipe(recipe);
		return new Response("Successful", 200);
	}
}
