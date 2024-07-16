package am.devvibes.buyandsell.entity.story;

import am.devvibes.buyandsell.entity.base.BaseEntityWithDates;
import am.devvibes.buyandsell.util.Status;
import jakarta.persistence.Column;
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

	@Column(nullable = false)
	private String userId;

	@Column(nullable = false)
	private String storyKey;

	private String caption;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

}
