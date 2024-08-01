package am.devvibes.buyandsell.dto.field;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldValueDto{

	private String value_eng;
	private String value_ru;
	private String value_hy;

}
