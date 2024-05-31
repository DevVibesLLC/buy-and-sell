package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.util.DescriptionNameEnum;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DescriptionEntity extends BaseEntity implements Serializable {

	@Enumerated(EnumType.STRING)
	private DescriptionNameEnum header;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "description_id")
	private List<FieldEntity> fields;

}