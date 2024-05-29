package am.devvibes.buyandsell.mapper.autoModel;

import am.devvibes.buyandsell.dto.autoModel.AutoModelDto;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutoModelMapperImpl implements AutoModelMapper {

	@Override
	public AutoModelDto mapEntityToDto(AutoModelEntity autoModelEntity) {
		return AutoModelDto.builder().id(autoModelEntity.getId()).model(autoModelEntity.getName()).build();
	}

	@Override
	public List<AutoModelDto> mapEntityListToDtoList(List<AutoModelEntity> autoModelEntityList) {
		return autoModelEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
