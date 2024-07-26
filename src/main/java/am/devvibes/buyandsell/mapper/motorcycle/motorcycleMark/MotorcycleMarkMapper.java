package am.devvibes.buyandsell.mapper.motorcycle.motorcycleMark;

import am.devvibes.buyandsell.dto.vehicle.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.entity.motorcycle.MotorcycleMarkEntity;

import java.util.List;

public interface MotorcycleMarkMapper {

	VehicleMarkDto mapEntityToDto(MotorcycleMarkEntity motorcycleMarkEntity);

	List<VehicleMarkDto> mapEntityListToDtoList(List<MotorcycleMarkEntity> motorcycleMarkEntityList);

}
