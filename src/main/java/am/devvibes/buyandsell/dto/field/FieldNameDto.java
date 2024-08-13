package am.devvibes.buyandsell.dto.field;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@Builder
public class FieldNameDto {

	private String fieldName_eng;

	private String fieldName_ru;

	private String fieldName_hy;
}
