package if3t.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.Id;

@Entity
@Table(name="triggers_ingredients")
public class TriggerIngredient {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_tri_ing", nullable = false)
	private Long id;
	
	@NotNull
	@ManyToOne
	@JsonBackReference(value="triggeringredient-recipe")
	@JoinColumn(name="id_recipe")
	private Recipe recipe;
	
	@NotNull
	@ManyToOne
	@JsonBackReference(value="triggeringredient-param")
	@JoinColumn(name="id_param")
	private ParametersTriggers param;
	
	@Column(name = "param_value", nullable = false)
	private String value;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

	public ParametersTriggers getParam() {
		return param;
	}

	public void setParam(ParametersTriggers param) {
		this.param = param;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((param == null) ? 0 : param.hashCode());
		result = prime * result + ((recipe == null) ? 0 : recipe.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TriggerIngredient))
			return false;
		TriggerIngredient other = (TriggerIngredient) obj;
		if (param == null) {
			if (other.param != null)
				return false;
		} else if (!param.equals(other.param))
			return false;
		if (recipe == null) {
			if (other.recipe != null)
				return false;
		} else if (!recipe.equals(other.recipe))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TriggerIngredient [id=" + id + ", recipe=" + recipe + ", param=" + param + ", value=" + value + "]";
	}
}