package am.devvibes.buyandsell.dto.field;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldRequestDto {

	private String fieldName;
	private List<String> value;
	private String measurement;

}
