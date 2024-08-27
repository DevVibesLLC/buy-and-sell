package am.devvibes.buyandsell.service.story;

import am.devvibes.buyandsell.dto.story.StoryRequestDto;
import am.devvibes.buyandsell.entity.story.StoryEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.story.StoryMapper;
import am.devvibes.buyandsell.repository.story.StoryRepository;
import am.devvibes.buyandsell.service.story.impl.StoryServiceImpl;
import am.devvibes.buyandsell.util.ExceptionConstants;
import am.devvibes.buyandsell.util.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StoryServiceTest {

	@Mock
	private StoryRepository storyRepository;

	@Mock
	private StoryMapper storyMapper;

	@InjectMocks
	private StoryServiceImpl storyService;

	@Test
	void saveStory__success() {

		StoryRequestDto storyRequestDto = StoryRequestDto.builder()
				.storyKey("story key")
				.caption("story caption")
				.build();

		StoryEntity storyEntity = StoryEntity.builder()
				.storyKey(storyRequestDto.getStoryKey())
				.caption(storyRequestDto.getCaption())
				.userId("userId")
				.status(Status.CREATED)
				.build();

		when(storyRepository.save(storyEntity)).thenReturn(storyEntity);
		when(storyMapper.mapDtoToEntity(storyRequestDto)).thenReturn(storyEntity);

		StoryEntity savedStory = storyService.saveStory(storyRequestDto);

		assertNotNull(savedStory);
		assertEquals(savedStory.getCaption(), storyRequestDto.getCaption());

		verify(storyRepository, times(1)).save(storyEntity);
		verify(storyMapper, times(1)).mapDtoToEntity(storyRequestDto);
	}

	@Test
	void findByUserId__success() {

		StoryEntity storyEntity = StoryEntity.builder()
				.storyKey("story key")
				.caption("story caption")
				.userId("userId")
				.status(Status.CREATED)
				.build();

		when(storyRepository.findByUserId("userId")).thenReturn(List.of(storyEntity));

		storyRepository.save(storyEntity);
		List<StoryEntity> storyEntities = storyService.getStoriesByUserId("userId");

		assertNotNull(storyEntities);
		assertEquals(1, storyEntities.size());

		verify(storyRepository, times(1)).findByUserId("userId");
	}

	@Test
	void deleteStory__success() {

		StoryEntity storyEntity = StoryEntity.builder()
				.storyKey("story key")
				.caption("story caption")
				.userId("userId")
				.status(Status.CREATED)
				.build();
		storyEntity.setId(1L);

		when(storyRepository.findById(storyEntity.getId())).thenReturn(Optional.of(storyEntity));
		when(storyRepository.save(storyEntity)).thenReturn(storyEntity);

		storyRepository.save(storyEntity);
		StoryEntity deletedStory = storyService.deleteStory(storyEntity.getId());

		assertNotNull(deletedStory);
		assertEquals(Status.DELETED, deletedStory.getStatus());

		verify(storyRepository, times(2)).save(storyEntity);
		verify(storyRepository, times(1)).findById(storyEntity.getId());
	}

	@Test
	void deleteStory__storyNotFound() {

		when(storyRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> storyService.deleteStory(1L), ExceptionConstants.STORY_NOT_FOUND.getString());

		verify(storyRepository, times(0)).save(any(StoryEntity.class));
		verify(storyRepository, times(1)).findById(1L);
	}
}
