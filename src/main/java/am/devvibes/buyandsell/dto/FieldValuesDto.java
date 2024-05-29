package am.devvibes.buyandsell.dto;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldValuesDto {

	private Long fieldId;
	private String fieldValue;

}
