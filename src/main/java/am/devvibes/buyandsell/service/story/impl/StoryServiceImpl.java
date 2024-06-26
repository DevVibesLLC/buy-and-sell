package am.devvibes.buyandsell.service.story.impl;

import am.devvibes.buyandsell.entity.story.StoryEntity;
import am.devvibes.buyandsell.exception.FileIsNullException;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.UnsupportedExtensionException;
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

	private final S3Service s3Service;
	private final SecurityService securityService;
	private final StoryRepository storyRepository;

	@Override
	public StoryEntity saveStory(MultipartFile story, String caption) {
		areAllFilesWithAllowedExtensions(story);
		String storyUrl = s3Service.uploadStory(story);
		StoryEntity storyEntity = StoryEntity.builder()
				.userId(securityService.getCurrentUserId())
				.storyUrl(storyUrl)
				.caption(caption)
				.status(Status.CREATED)
				.build();

		return storyRepository.save(storyEntity);
	}

	@Override
	public List<StoryEntity> getStoriesByUserId(String userId) {
		List<StoryEntity> usersStories = storyRepository.findByUserId(userId);
		return usersStories.stream().filter(s -> s.getStatus().equals(Status.CREATED)).toList();
	}

	@Override
	public void deleteStory(Long storyId) {
		StoryEntity storyEntity = storyRepository.findById(storyId)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.STORY_NOT_FOUND));

		storyEntity.setStatus(Status.DELETED);
		storyRepository.save(storyEntity);
	}

	private void areAllFilesWithAllowedExtensions(MultipartFile story) {
		List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "svg", "mp4");

		String fileName = story.getOriginalFilename();
		if (!isNull(fileName)) {
			String extension = getFileExtension(fileName);
			if (!allowedExtensions.contains(extension.toLowerCase())) {
				throw new UnsupportedExtensionException(ExceptionConstants.UNSUPPORTED_FILE_EXTENSION);
			}
		} else {
			throw new FileIsNullException(ExceptionConstants.FILE_NAME_IS_NULL);
		}
	}

	private String getFileExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
			return fileName.substring(dotIndex + 1);
		} else {
			return "";
		}
	}
}
