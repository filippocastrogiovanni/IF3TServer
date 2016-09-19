package if3t.services;

import java.util.List;
import java.util.Map;

import if3t.entities.ParametersKeyword;
import if3t.entities.User;

public interface ParameterKeywordService 
{	
	public void addParametersKeyword(ParametersKeyword parametersKeyword);
	public ParametersKeyword readParameterKeyword(Long id);
	public List<ParametersKeyword> readParametersKeyword();
	public List<ParametersKeyword> getRecipeParametersKeyword(Long recipeId);
	public Map<Long, ParametersKeyword> getRecipeParametersKeywordMap(Long recipeId);
}
