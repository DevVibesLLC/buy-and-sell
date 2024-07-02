package am.devvibes.buyandsell.mapper.story;

import am.devvibes.buyandsell.dto.story.StoryRequestDto;
import am.devvibes.buyandsell.dto.story.StoryResponseDto;
import am.devvibes.buyandsell.entity.story.StoryEntity;

import java.util.List;

public interface StoryMapper {

	StoryEntity mapDtoToEntity(StoryRequestDto storyRequestDto);

	StoryResponseDto mapEntityToDto(StoryEntity storyEntity);

	List<StoryResponseDto> mapEntityListToDtoList(List<StoryEntity> storyEntities);

}
