package am.devvibes.buyandsell.service.story.impl;

import am.devvibes.buyandsell.dto.story.StoryRequestDto;
import am.devvibes.buyandsell.entity.story.StoryEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.story.StoryMapper;
import am.devvibes.buyandsell.repository.story.StoryRepository;
import am.devvibes.buyandsell.service.story.StoryService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import am.devvibes.buyandsell.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

	private final StoryRepository storyRepository;
	private final StoryMapper storyMapper;

	@Override
	public StoryEntity saveStory(StoryRequestDto storyRequestDto) {
		StoryEntity storyEntity = storyMapper.mapDtoToEntity(storyRequestDto);
		return storyRepository.save(storyEntity);
	}

	@Override
	public List<StoryEntity> getStoriesByUserId(String userId) {
		List<StoryEntity> usersStories = storyRepository.findByUserId(userId);
		return usersStories.stream().filter(s -> s.getStatus().equals(Status.CREATED)).toList();
	}

	@Override
	@Transactional
	public StoryEntity deleteStory(Long storyId) {
		StoryEntity storyEntity = storyRepository.findById(storyId)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.STORY_NOT_FOUND));

		storyEntity.setStatus(Status.DELETED);
		return storyRepository.save(storyEntity);
	}

}
