package am.devvibes.buyandsell.mapper.bus.busModel;

import am.devvibes.buyandsell.dto.vehicleModel.VehicleModelDto;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.bus.BusModelEntity;

import java.util.List;

public interface BusModelMapper {

	VehicleModelDto mapEntityToDto(BusModelEntity busModelEntity);

	List<VehicleModelDto> mapEntityListToDtoList(List<BusModelEntity> busModelEntityList);

}
