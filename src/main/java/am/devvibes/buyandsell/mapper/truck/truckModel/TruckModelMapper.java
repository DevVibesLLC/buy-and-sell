package am.devvibes.buyandsell.mapper.truck.truckModel;

import am.devvibes.buyandsell.dto.vehicle.vehicleModel.VehicleModelDto;
import am.devvibes.buyandsell.entity.truck.TruckModelEntity;

import java.util.List;

public interface TruckModelMapper {

	VehicleModelDto mapEntityToDto(TruckModelEntity truckModelEntity);

	List<VehicleModelDto> mapEntityListToDtoList(List<TruckModelEntity> truckModelEntityList);

}
