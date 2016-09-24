package if3t.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import if3t.entities.Action;
import if3t.entities.ActionIngredient;
import if3t.entities.ParametersActions;
import if3t.entities.ParametersTriggers;
import if3t.entities.Recipe;
import if3t.entities.Trigger;
import if3t.entities.TriggerIngredient;
import if3t.entities.User;
import if3t.exceptions.AddRecipeException;
import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.NoPermissionException;
import if3t.exceptions.NotFoundRecipeException;
import if3t.exceptions.NotLoggedInException;
import if3t.exceptions.PartialUpdateException;
import if3t.models.ActionPOJO;
import if3t.models.RecipePOJO;
import if3t.models.Response;
import if3t.models.TriggerPOJO;
import if3t.services.ActionIngredientService;
import if3t.services.CreateRecipeService;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;
import if3t.services.UserService;

@RestController
@CrossOrigin
public class RecipeController 
{
	@Autowired
	private RecipeService recipeService;
	@Autowired
	private UserService userService;
	@Autowired
	private CreateRecipeService createRecipeService;
	@Autowired
	private TriggerIngredientService triggerIngrService;
	@Autowired
	private ActionIngredientService actionIngrService;
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/user_recipes", method=RequestMethod.GET)
	public List<Recipe> getUserRecipes() throws NotLoggedInException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not loggedIn");
		}
		
		return recipeService.readUserRecipes(userService.getUserByUsername(auth.getName()).getId());
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/public_recipes", method=RequestMethod.GET)
	public List<Recipe> getPublicRecipes() {
		return recipeService.readPublicRecipes();
	}
	
	//FIXME mettere tutto in una funzione del service per garantire la transazionalità
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/recipe/{id}", method=RequestMethod.GET)
	public RecipePOJO readRecipe(@PathVariable Long id) throws NotLoggedInException, NoPermissionException, NotFoundRecipeException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not loggedIn");
		}
		
		User loggedUser = userService.getUserByUsername(auth.getName());
		List<Recipe> recList = recipeService.readRecipe(id, loggedUser);
		Trigger trig = recList.get(0).getTrigger();
		List<ParametersTriggers> ptList = createRecipeService.readChannelParametersTriggers(trig.getId(), trig.getChannel().getChannelId());
		Map<Long, TriggerIngredient> tiMap = triggerIngrService.getRecipeTriggerIngredientsMap(recList.get(0).getGroupId());
		TriggerPOJO trigPOJO = new TriggerPOJO(trig, ptList, tiMap);
		List<ActionPOJO> actPOJOList = new ArrayList<ActionPOJO>();
		
		for (Recipe rec : recipeService.readRecipe(id, loggedUser))
		{
			Action act = rec.getAction();
			List<ParametersActions> paList = createRecipeService.readChannelParametersActions(act.getId(), act.getChannel().getChannelId());
			Map<Long, ActionIngredient> aiMap = actionIngrService.getRecipeActionIngredientsMap(rec.getId());
			actPOJOList.add(new ActionPOJO(act, paList, aiMap));
		}		
		
		return new RecipePOJO(recipeService.readRecipe(id, loggedUser), trigPOJO, actPOJOList);
	}
	
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/add_recipe", method=RequestMethod.POST)
	public Response addRecipe(@RequestBody Recipe recipe) throws NotLoggedInException, AddRecipeException 
	{		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not logged in!");
		}
		
		List<Recipe> recipes = new ArrayList<Recipe>();
		recipes.add(recipe);
		
		recipeService.addRecipe(recipes, userService.getUserByUsername(auth.getName()));
		return new Response("The recipe has been created successfully", HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase());
	}

	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/delete_recipe/{id}", method=RequestMethod.DELETE)
	public Response deleteRecipe(@PathVariable Long id) throws NotLoggedInException, NoPermissionException, NotFoundRecipeException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not loggedIn");
		}
		
		recipeService.deleteRecipe(id, userService.getUserByUsername(auth.getName()));
		return new Response("The recipe has been deleted successfully", HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/publish_recipe/", method=RequestMethod.PUT)
	public Response publishRecipe(@RequestBody Recipe recipe) throws NotLoggedInException, NoPermissionException, NotFoundRecipeException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not loggedIn");
		}
		
		User user = userService.getUserByUsername(auth.getName());
		List<Recipe> recipeList = recipeService.readRecipe(recipe.getId(), user);
		
		//Lo user è lo stesso (se non ci sono falle) in tutte le recipe (in cui cambia solo la action) quindi faccio un solo check
		if (!recipeList.get(0).getUser().equals(user)) {
			throw new NoPermissionException();
		}
				
		for (Recipe rec : recipeList)
		{
			recipeService.toggleIsPublicRecipe(rec);
		}
				
		String state = recipeList.get(0).getIsPublic() ? "published" : "unpublished";
		return new Response("The recipe has been " + state + " successfully", HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
	}
	
	//TODO da testare con canali abilitati
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/enable_recipe/", method=RequestMethod.PUT)
	public Response enableRecipe(@RequestBody Recipe recipe) throws NotLoggedInException, ChannelNotAuthorizedException, NoPermissionException, NotFoundRecipeException 
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not loggedIn");
		}
		
		User user = userService.getUserByUsername(auth.getName());
		List<Recipe> recipeList = recipeService.readRecipe(recipe.getId(), user);
		
		//Lo user è lo stesso (se non ci sono falle) in tutte le recipe (in cui cambia solo la action) quindi faccio un solo check
		if (!recipeList.get(0).getUser().equals(user)) {
			throw new NoPermissionException();
		}
		
		recipeService.toggleIsEnabledRecipe(recipeList, user);
		String state = recipeList.get(0).getIsEnabled() ? "enabled" : "disabled";
		return new Response("The recipe has been " + state + " successfully", HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
	}
	
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value="/update_recipe/", method=RequestMethod.PUT)
	public Response updateRecipe(@Validated @RequestBody RecipePOJO recipe) throws NotLoggedInException, NoPermissionException, NotFoundRecipeException, PartialUpdateException
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			
		if (auth == null) {
			throw new NotLoggedInException("ERROR: not loggedIn");
		}
			
		if (!recipe.getUsername().equals(auth.getName())) {
			throw new NoPermissionException("ERROR: You don't have permissions to perform this action!");
		}
			
		recipeService.updateRecipe(recipe);		
		return new Response("The recipe has been updated successfully", HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
	}
}