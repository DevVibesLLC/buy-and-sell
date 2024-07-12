package am.devvibes.buyandsell.mapper.motorcycle.motorcycleMark;

import am.devvibes.buyandsell.dto.vehicle.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.entity.motorcycle.MotorcycleMarkEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MotorcycleMarkMapperImpl implements MotorcycleMarkMapper {

	@Override
	public VehicleMarkDto mapEntityToDto(MotorcycleMarkEntity motorcycleMarkEntity) {
		return VehicleMarkDto.builder().id(motorcycleMarkEntity.getId()).mark(motorcycleMarkEntity.getName()).build();
	}

	@Override
	public List<VehicleMarkDto> mapEntityListToDtoList(List<MotorcycleMarkEntity> motorcycleMarkEntityList) {
		return motorcycleMarkEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
