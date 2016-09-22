package if3t.timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import if3t.apis.WeatherUtil;
import if3t.apis.WeatherUtil.UnitsFormat;
import if3t.entities.Recipe;
import if3t.services.RecipeService;
import if3t.services.TriggerIngredientService;

@Component
public class WeatherTasks 
{
	@Autowired
	private WeatherUtil weatherUtil;
	@Autowired
    private RecipeService recipeService;
	@Autowired
	private TriggerIngredientService triggerIngrService;
	
	//TODO sistemare questo valore se serve
	@Scheduled(fixedRateString = "${app.scheduler.value}")
	public void weatherSunriseScheduler()
	{
//		weatherUtil.getCurrentWeatherAtSunrise(670801L, 11L, UnitsFormat.CELSIUS);
//		
//		
//		for (Recipe recipe : recipeService.getEnabledRecipesByTriggerChannel("weather"))
//		{
//			
//		}
	}
}
