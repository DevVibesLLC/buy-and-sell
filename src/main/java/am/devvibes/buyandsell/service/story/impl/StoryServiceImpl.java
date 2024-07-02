package am.devvibes.buyandsell.service.story.impl;

import am.devvibes.buyandsell.dto.story.StoryRequestDto;
import am.devvibes.buyandsell.dto.story.StoryResponseDto;
import am.devvibes.buyandsell.entity.story.StoryEntity;
import am.devvibes.buyandsell.exception.FileIsNullException;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.UnsupportedExtensionException;
import am.devvibes.buyandsell.mapper.story.StoryMapper;
import am.devvibes.buyandsell.repository.story.StoryRepository;
import am.devvibes.buyandsell.service.s3.S3Service;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.story.StoryService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import am.devvibes.buyandsell.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

	private final StoryRepository storyRepository;
	private final StoryMapper storyMapper;

	@Override
	public StoryResponseDto saveStory(StoryRequestDto storyRequestDto) {
		StoryEntity storyEntity = storyMapper.mapDtoToEntity(storyRequestDto);
		StoryEntity saved = storyRepository.save(storyEntity);
		return storyMapper.mapEntityToDto(saved);
	}

	@Override
	public List<StoryResponseDto> getStoriesByUserId(String userId) {
		List<StoryEntity> usersStories = storyRepository.findByUserId(userId);
		List<StoryEntity> storyEntities = usersStories.stream().filter(s -> s.getStatus().equals(Status.CREATED)).toList();
		return storyMapper.mapEntityListToDtoList(storyEntities);
	}

	@Override
	public void deleteStory(Long storyId) {
		StoryEntity storyEntity = storyRepository.findById(storyId)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.STORY_NOT_FOUND));

		storyEntity.setStatus(Status.DELETED);
		storyRepository.save(storyEntity);
	}

}
