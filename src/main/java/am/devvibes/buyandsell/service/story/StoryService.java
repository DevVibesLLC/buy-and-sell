package am.devvibes.buyandsell.service.story;

import am.devvibes.buyandsell.dto.story.StoryRequestDto;
import am.devvibes.buyandsell.dto.story.StoryResponseDto;
import am.devvibes.buyandsell.entity.story.StoryEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StoryService {

	StoryResponseDto saveStory(StoryRequestDto storyRequestDto);

	List<StoryResponseDto> getStoriesByUserId(String userId);

	void deleteStory(Long storyId);

}
