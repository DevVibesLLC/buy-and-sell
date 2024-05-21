package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.entity.abstracts.AbstractCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "categories")
public class CategoryEntity extends AbstractCategory {

	@Builder
	public CategoryEntity(Long id, String category) {
		super(id,category);
	}

}
