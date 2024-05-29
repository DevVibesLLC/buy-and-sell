package am.devvibes.buyandsell.mapper.autoModel;

import am.devvibes.buyandsell.dto.autoModel.AutoModelDto;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;

import java.util.List;

public interface AutoModelMapper {

	AutoModelDto mapEntityToDto(AutoModelEntity autoModelEntity);

	List<AutoModelDto> mapEntityListToDtoList(List<AutoModelEntity> autoModelEntityList);

}
