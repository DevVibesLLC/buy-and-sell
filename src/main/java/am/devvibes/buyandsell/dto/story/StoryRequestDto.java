package am.devvibes.buyandsell.dto.story;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryRequestDto {

	@NotBlank
	private String caption;

	@NotBlank
	private String storyKey;

}
