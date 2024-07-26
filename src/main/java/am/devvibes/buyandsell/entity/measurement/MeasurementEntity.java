package am.devvibes.buyandsell.entity.measurement;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeasurementEntity extends BaseEntity implements Serializable {

	@Column(nullable = false)
	private String symbol;

	@Column(nullable = false)
	private String category;

}