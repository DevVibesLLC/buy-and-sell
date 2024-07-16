package am.devvibes.buyandsell.dto.value;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldValuesDto {

	@NotNull
	private Long fieldId;

	@NotBlank
	private String fieldName;

	@NotBlank
	private String fieldValue;

}
