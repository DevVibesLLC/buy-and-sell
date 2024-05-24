package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import am.devvibes.buyandsell.util.CategoryEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEntity extends BaseEntity {

	private CategoryEnum name;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<DescriptionEntity> descriptions;

}
