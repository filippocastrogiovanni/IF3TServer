package if3t.repositories;

import java.util.List;

import if3t.models.ParametersKeyword;

public interface ParameterKeywordRepository {

	public Iterable<ParametersKeyword> findAll();
	public ParametersKeyword findOne(Long id);
	public List<ParametersKeyword> findByRecipe_Id(Long recipeId);
	public ParametersKeyword save(ParametersKeyword ai);
	//FIXME forse il parametro per cancellare è l'id
	public void delete(ParametersKeyword ai);
	
}
