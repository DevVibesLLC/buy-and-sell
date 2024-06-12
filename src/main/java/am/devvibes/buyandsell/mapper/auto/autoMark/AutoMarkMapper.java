package am.devvibes.buyandsell.mapper.auto.autoMark;

import am.devvibes.buyandsell.dto.autoMark.VehicleMarkDto;
import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;

import java.util.List;

public interface AutoMarkMapper {

	VehicleMarkDto mapEntityToDto(AutoMarkEntity autoMarkEntity);

	List<VehicleMarkDto> mapEntityListToDtoList(List<AutoMarkEntity> autoMarkEntityList);

}
