package am.devvibes.buyandsell.dto.generation;

import lombok.*;

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
