package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import am.devvibes.buyandsell.util.DescriptionNameEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DescriptionEntity extends BaseEntity {

	private DescriptionNameEnum header;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<FieldEntity> fields;

}
