package am.devvibes.buyandsell.mapper.auto.autoModel;

import am.devvibes.buyandsell.dto.autoModel.VehicleModelDto;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;

import java.util.List;

public interface AutoModelMapper {

	VehicleModelDto mapEntityToDto(AutoModelEntity autoModelEntity);

	List<VehicleModelDto> mapEntityListToDtoList(List<AutoModelEntity> autoModelEntityList);

}
