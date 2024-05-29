package am.devvibes.buyandsell.mapper.generation;

import am.devvibes.buyandsell.dto.generation.GenerationDto;
import am.devvibes.buyandsell.dto.generation.GenerationItemDto;
import am.devvibes.buyandsell.entity.auto.GenerationEntity;
import am.devvibes.buyandsell.entity.auto.GenerationItemEntity;

import java.util.List;

public interface GenerationMapper {

	GenerationDto mapEntityToDto(GenerationEntity generationEntity);

	List<GenerationDto> mapEntityListToDtoList(List<GenerationEntity> generationEntityList);

	GenerationItemDto mapEntityItemToDtoItem(GenerationItemEntity generationItemEntity);

	List<GenerationItemDto> mapDtoListToEntityList(List<GenerationItemEntity> generationItemEntities);

}
