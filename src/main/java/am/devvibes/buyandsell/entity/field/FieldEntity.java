package am.devvibes.buyandsell.entity.field;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldEntity extends BaseEntity{

	@Column(nullable = false)
	private String fieldValue_eng;

	@Column(nullable = false)
	private String fieldValue_ru;

	@Column(nullable = false)
	private String fieldValue_hy;

	@ManyToOne
	@JoinColumn(nullable = false)
	private FieldNameEntity fieldName;

}