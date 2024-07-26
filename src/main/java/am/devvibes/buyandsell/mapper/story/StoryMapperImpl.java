package am.devvibes.buyandsell.mapper.story;

import am.devvibes.buyandsell.dto.story.StoryRequestDto;
import am.devvibes.buyandsell.dto.story.StoryResponseDto;
import am.devvibes.buyandsell.entity.story.StoryEntity;
import am.devvibes.buyandsell.service.s3.impl.S3ServiceImpl;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryMapperImpl implements StoryMapper {

	private final S3ServiceImpl s3Service;
	private final SecurityService securityService;

	@Override
	public StoryEntity mapDtoToEntity(StoryRequestDto storyRequestDto) {
		return StoryEntity.builder()
				.userId(securityService.getCurrentUserId())
				.storyKey(storyRequestDto.getStoryKey())
				.caption(storyRequestDto.getCaption())
				.status(Status.CREATED)
				.build();
	}

	@Override
	public StoryResponseDto mapEntityToDto(StoryEntity storyEntity) {
		return StoryResponseDto.builder()
				.id(storyEntity.getId())
				.caption(storyEntity.getCaption())
				.url(s3Service.getStoryPresignedDownloadUrl(storyEntity.getStoryKey()))
				.userId(storyEntity.getUserId())
				.build();
	}

	@Override
	public List<StoryResponseDto> mapEntityListToDtoList(List<StoryEntity> storyEntities) {
		return storyEntities.stream().map(this::mapEntityToDto).toList();
	}

}
