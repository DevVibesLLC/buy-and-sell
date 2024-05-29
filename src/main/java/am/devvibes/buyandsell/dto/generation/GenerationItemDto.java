package am.devvibes.buyandsell.dto.generation;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenerationItemDto{

	private Long id;
	private int restyling;
	private int yearStart;
	private Integer yearEnd;
	private List<String> frames;
}
