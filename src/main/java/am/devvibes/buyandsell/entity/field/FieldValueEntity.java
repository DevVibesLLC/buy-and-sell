package am.devvibes.buyandsell.entity.field;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldValueEntity extends BaseEntity {

	private String value_eng;
	private String value_ru;
	private String value_hy;

}
