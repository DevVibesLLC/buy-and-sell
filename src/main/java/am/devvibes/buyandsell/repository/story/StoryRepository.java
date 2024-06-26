package am.devvibes.buyandsell.repository.story;

import am.devvibes.buyandsell.entity.story.StoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface StoryRepository extends JpaRepository<StoryEntity, Long> {

	List<StoryEntity> findByUserId(String userId);

	List<StoryEntity> findByCreatedAtBefore(ZonedDateTime createdAt);
}