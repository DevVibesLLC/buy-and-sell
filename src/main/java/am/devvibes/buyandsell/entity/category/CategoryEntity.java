package am.devvibes.buyandsell.entity.category;

import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.entity.bus.BusMarkEntity;
import am.devvibes.buyandsell.entity.description.DescriptionEntity;
import am.devvibes.buyandsell.entity.truck.TruckMarkEntity;
import am.devvibes.buyandsell.util.CategoryEnum;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEntity extends BaseEntity implements Serializable {

	@Enumerated(EnumType.STRING)
	private CategoryEnum name;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "category_id")
	private List<DescriptionEntity> descriptions;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private List<AutoMarkEntity> autoMarks;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private List<TruckMarkEntity> truckMarks;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private List<BusMarkEntity> busMarks;

}
