package am.devvibes.buyandsell.classes.value;

import am.devvibes.buyandsell.util.MeasurementEnum;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ValueAndMeasurement {

	private String value;

	private MeasurementEnum measurement;

}
