package am.devvibes.buyandsell.dto.generation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenerationDto {

	private Long id;
	private Integer generationNumber;
	private List<GenerationItemDto> generationItems;

}
