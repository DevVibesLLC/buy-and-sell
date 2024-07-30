package am.devvibes.buyandsell.dto.value;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldValuesDto {

	@NotNull
	private Long fieldId;

	@NotBlank
	private String fieldName_eng;

	@NotBlank
	private String fieldName_ru;

	@NotBlank
	private String fieldName_hy;

	@NotBlank
	private String fieldValue;

}
