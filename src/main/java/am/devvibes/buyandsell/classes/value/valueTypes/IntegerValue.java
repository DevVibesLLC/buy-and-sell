package am.devvibes.buyandsell.classes.value.valueTypes;

import am.devvibes.buyandsell.classes.value.Value;
import am.devvibes.buyandsell.classes.value.ValueAndMeasurement;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class IntegerValue extends Value {

	private Integer value;

}
