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
public class MeasurementEntity extends BaseEntity{

	@Column(nullable = false)
	private String symbol_eng;

	@Column(nullable = false)
	private String symbol_ru;

	@Column(nullable = false)
	private String symbol_hy;

	@Column(nullable = false)
	private String category;

}