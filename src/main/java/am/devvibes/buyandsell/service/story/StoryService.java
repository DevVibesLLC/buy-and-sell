package am.devvibes.buyandsell.service.story;

import am.devvibes.buyandsell.entity.story.StoryEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StoryService {

	StoryEntity saveStory(MultipartFile story, String caption);

	List<StoryEntity> getStoriesByUserId(String userId);

	void deleteStory(Long storyId);

}
