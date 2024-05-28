package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import am.devvibes.buyandsell.util.CategoryEnum;
import jakarta.persistence.*;
import lombok.*;
import org.jboss.resteasy.spi.touri.MappedBy;

import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEntity extends BaseEntity {

	@Enumerated(EnumType.STRING)
	private CategoryEnum name;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "category_id")
	private List<DescriptionEntity> descriptions;

	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name = "category_id")
	private List<AutoMarkEntity> marks;

}
