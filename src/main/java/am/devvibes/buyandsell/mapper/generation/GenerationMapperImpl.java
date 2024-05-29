package am.devvibes.buyandsell.mapper.generation;

import am.devvibes.buyandsell.dto.generation.GenerationDto;
import am.devvibes.buyandsell.dto.generation.GenerationItemDto;
import am.devvibes.buyandsell.entity.auto.GenerationEntity;
import am.devvibes.buyandsell.entity.auto.GenerationItemEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenerationMapperImpl implements GenerationMapper {

	@Override
	public GenerationDto mapEntityToDto(GenerationEntity generationEntity) {
		return GenerationDto.builder()
				.id(generationEntity.getId())
				.generationNumber(generationEntity.getGenerationNumber())
				.generationItems(mapDtoListToEntityList(generationEntity.getItems()))
				.build();
	}

	@Override
	public List<GenerationDto> mapEntityListToDtoList(List<GenerationEntity> generationEntityList) {
		return generationEntityList.stream().map(this::mapEntityToDto).toList();
	}

	@Override
	public GenerationItemDto mapEntityItemToDtoItem(GenerationItemEntity generationItemEntity) {
		return GenerationItemDto.builder()
				.id(generationItemEntity.getId())
				.restyling(generationItemEntity.getRestyling())
				.yearStart(generationItemEntity.getYearStart())
				.yearEnd(generationItemEntity.getYearEnd())
				.frames(generationItemEntity.getFrames())
				.build();
	}

	@Override
	public List<GenerationItemDto> mapDtoListToEntityList(List<GenerationItemEntity> generationItemEntities) {
		return generationItemEntities.stream().map(this::mapEntityItemToDtoItem).toList();
	}

}
