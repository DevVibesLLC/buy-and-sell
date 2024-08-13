package am.devvibes.buyandsell.dto.value;

import am.devvibes.buyandsell.dto.field.FieldNameDto;
import am.devvibes.buyandsell.dto.field.FieldValueDto;
import jakarta.persistence.Embedded;
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

	@Embedded
	private FieldNameDto fieldNames;

	@Embedded
	private FieldValueDto fieldValues;

}
