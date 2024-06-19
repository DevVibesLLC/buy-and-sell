package am.devvibes.buyandsell.mapper.bus.busMark;

import am.devvibes.buyandsell.dto.vehicle.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.entity.bus.BusMarkEntity;

import java.util.List;

public interface BusMarkMapper {

	VehicleMarkDto mapEntityToDto(BusMarkEntity autoMarkEntity);

	List<VehicleMarkDto> mapEntityListToDtoList(List<BusMarkEntity> autoMarkEntityList);
	

}
