package am.devvibes.buyandsell.mapper.bus.busModel;

import am.devvibes.buyandsell.dto.vehicle.vehicleModel.VehicleModelDto;
import am.devvibes.buyandsell.entity.bus.BusModelEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusModelMapperImpl implements BusModelMapper {

	@Override
	public VehicleModelDto mapEntityToDto(BusModelEntity busModelEntity) {
		return VehicleModelDto.builder().id(busModelEntity.getId()).model(busModelEntity.getName()).build();
	}

	@Override
	public List<VehicleModelDto> mapEntityListToDtoList(List<BusModelEntity> busModelEntityList) {
		return busModelEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
