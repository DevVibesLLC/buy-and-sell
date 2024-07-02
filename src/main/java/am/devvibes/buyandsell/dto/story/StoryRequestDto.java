package am.devvibes.buyandsell.dto.story;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryRequestDto {

	private String caption;
	private String storyKey;

}
