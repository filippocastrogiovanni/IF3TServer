package if3t.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import if3t.models.ParametersKeyword;
import if3t.repositories.ParameterKeywordRepository;
import if3t.repositories.ParameterKeywordRepository;

@Service
@Transactional
public class ParameterKeywordServiceImpl implements ParameterKeywordService
{
	@Autowired
	private ParameterKeywordRepository parameterKeywordRepo;
	
	@Override
	public ParametersKeyword readParameterKeyword(Long id) {
		return parameterKeywordRepo.findOne(id);
	}

	@Override
	public List<ParametersKeyword> readParametersKeyword() 
	{
		List<ParametersKeyword> list = new ArrayList<ParametersKeyword>();
		
		for (ParametersKeyword t: parameterKeywordRepo.findAll())
		{
			list.add(t);
		}
		
		return list;
	}

	@Override
	public List<ParametersKeyword> getRecipeParametersKeyword(Long recipeId) {
		return parameterKeywordRepo.findByRecipe_Id(recipeId);
	}

	@Override
	public Map<Long, ParametersKeyword> getRecipeParametersKeywordMap(Long recipeId) 
	{
		Map<Long, ParametersKeyword> map = new HashMap<Long, ParametersKeyword>();
		
		for (ParametersKeyword ai : parameterKeywordRepo.findByRecipe_Id(recipeId))
		{
			map.putIfAbsent(ai.getId(), ai);
		}
		
		return map;
	}

	@Override
	public void addParametersKeyword(ParametersKeyword parametersKeyword) {
		parameterKeywordRepo.save(parametersKeyword);
	}
}