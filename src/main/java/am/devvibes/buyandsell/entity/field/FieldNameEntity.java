package am.devvibes.buyandsell.entity.field;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.entity.measurement.MeasurementEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldNameEntity extends BaseEntity{

	@Column(nullable = false)
	private String fieldName_eng;

	@Column(nullable = false)
	private String fieldName_ru;

	@Column(nullable = false)
	private String fieldName_hy;

	private boolean isRequired;

	private boolean isPrefilled;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "field_name_entity_id")
	private List<FieldValueEntity> values;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "measurement_id")
	private MeasurementEntity measurement;

}
