package am.devvibes.buyandsell.dto.field;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldRequestDto {

	private String fieldName;
	private String value;
	private String measurement;

}
