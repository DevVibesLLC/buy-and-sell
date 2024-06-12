package am.devvibes.buyandsell.mapper.truck.truckMark;

import am.devvibes.buyandsell.dto.autoMark.VehicleMarkDto;
import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.truck.TruckMarkEntity;

import java.util.List;

public interface TruckMarkMapper {

	VehicleMarkDto mapEntityToDto(TruckMarkEntity truckMarkEntity);

	List<VehicleMarkDto> mapEntityListToDtoList(List<TruckMarkEntity> truckMarkEntityList);

}
