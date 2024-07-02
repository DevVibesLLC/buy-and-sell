package am.devvibes.buyandsell.dto.story;

import lombok.*;

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
