package am.devvibes.buyandsell.dto.generation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenerationItemDto {

	private Long id;
	private int restyling;
	private int yearStart;
	private Integer yearEnd;
	private List<String> frames;

}
