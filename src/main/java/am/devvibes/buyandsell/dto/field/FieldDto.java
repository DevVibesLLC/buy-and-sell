package am.devvibes.buyandsell.dto.field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldDto {

	private String fieldName;
	private List<String> value;
	private String measurement;

}
