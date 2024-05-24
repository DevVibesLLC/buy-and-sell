package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.classes.value.ValueAndMeasurement;
import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import am.devvibes.buyandsell.util.FieldNameEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class FieldEntity extends BaseEntity {

	private FieldNameEnum fieldName;

	@Embedded
	private ValueAndMeasurement fieldValue;

}
