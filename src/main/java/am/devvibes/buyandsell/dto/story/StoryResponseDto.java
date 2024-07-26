package am.devvibes.buyandsell.dto.story;

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
public class StoryResponseDto {

	private Long id;
	private String caption;
	private String url;
	private String userId;

}
