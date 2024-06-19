package am.devvibes.buyandsell.mapper.truck.truckModel;

import am.devvibes.buyandsell.dto.vehicle.vehicleModel.VehicleModelDto;
import am.devvibes.buyandsell.entity.truck.TruckModelEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TruckModelMapperImpl implements TruckModelMapper {

	@Override
	public VehicleModelDto mapEntityToDto(TruckModelEntity truckModelEntity) {
		return VehicleModelDto.builder().id(truckModelEntity.getId()).model(truckModelEntity.getName()).build();
	}

	@Override
	public List<VehicleModelDto> mapEntityListToDtoList(List<TruckModelEntity> truckModelEntityList) {
		return truckModelEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
