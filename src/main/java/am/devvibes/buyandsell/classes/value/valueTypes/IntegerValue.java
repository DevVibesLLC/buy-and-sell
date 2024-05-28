package am.devvibes.buyandsell.classes.value.valueTypes;

import am.devvibes.buyandsell.classes.value.Value;
import jakarta.persistence.Embeddable;
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
