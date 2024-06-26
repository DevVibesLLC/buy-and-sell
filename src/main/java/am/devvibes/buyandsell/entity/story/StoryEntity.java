package am.devvibes.buyandsell.entity.story;

import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import am.devvibes.buyandsell.util.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryEntity extends BaseEntityWithDates {

	private String userId;

	private String storyUrl;

	private String caption;

	@Enumerated(EnumType.STRING)
	private Status status;

}
