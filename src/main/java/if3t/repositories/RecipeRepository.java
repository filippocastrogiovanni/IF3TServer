package if3t.repositories;

import java.util.List;
import org.springframework.data.repository.Repository;
import if3t.models.Recipe;

public interface RecipeRepository extends Repository<Recipe, Long> {

	public Iterable<Recipe> findAll();
	/* 	findOne and findByGroupId must be used together if we need to find one recipe 
	  	because we can have more recipe with different Id but with same groupId that 
	  	correspond to only one recipe with more than one action */
	public Recipe findOne(Long id);
	public List<Recipe> findByIsEnabledAndTrigger_Channel_Keyword(Boolean isEnabled, String channelKeyword);
	public List<Recipe> findByGroupId(String groupId);
	public List<Recipe> findByUser_Id(Long userId);
	public List<Recipe> findByIsPublic(Boolean isPublic);
	public Recipe save(Recipe booking);
	//FIXME forse il parametro per cancellare è l'id
	public void delete(Recipe booking);
}
