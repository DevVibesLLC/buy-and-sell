package am.devvibes.buyandsell.service.story;

import am.devvibes.buyandsell.dto.story.StoryRequestDto;
import am.devvibes.buyandsell.entity.story.StoryEntity;

import java.util.List;

public interface StoryService {

	StoryEntity saveStory(StoryRequestDto storyRequestDto);

	List<StoryEntity> getStoriesByUserId(String userId);

	void deleteStory(Long storyId);

}
