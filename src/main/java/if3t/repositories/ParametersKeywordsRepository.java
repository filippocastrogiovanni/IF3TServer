package if3t.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;

import if3t.entities.ParametersKeyword;

public interface ParametersKeywordsRepository extends Repository<ParametersKeyword, Long>{

	public Iterable<ParametersKeyword> findAll();
	public ParametersKeyword findOne(Long id);
	public List<ParametersKeyword> findByRecipe_Id(Long recipeId);
	public ParametersKeyword save(ParametersKeyword ai);
	//FIXME forse il parametro per cancellare è l'id
	public void delete(ParametersKeyword ai);
	
}
