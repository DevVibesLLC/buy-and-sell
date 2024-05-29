package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueEntity extends BaseEntity {

	private String fieldValue;

	@ManyToOne
	private FieldEntity field;

}