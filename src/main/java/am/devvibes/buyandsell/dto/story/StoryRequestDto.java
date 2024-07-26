package am.devvibes.buyandsell.dto.story;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
