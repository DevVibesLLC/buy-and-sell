package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldNameEntity extends BaseEntity implements Serializable {

	private String fieldName;

	private boolean isRequired;

	private boolean isPrefilled;

	@ElementCollection
	private List<String> value;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "measurement_id")
	private MeasurementEntity measurement;

}
