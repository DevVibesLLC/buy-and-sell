package am.devvibes.buyandsell.mapper.truck.truckMark;

import am.devvibes.buyandsell.dto.vehicle.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.entity.truck.TruckMarkEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TruckMarkMapperImpl implements TruckMarkMapper {

	@Override
	public VehicleMarkDto mapEntityToDto(TruckMarkEntity truckMarkEntity) {
		return VehicleMarkDto.builder()
				.id(truckMarkEntity.getId())
				.mark(truckMarkEntity.getName())
				.build();
	}

	@Override
	public List<VehicleMarkDto> mapEntityListToDtoList(List<TruckMarkEntity> truckMarkEntityList) {
		return truckMarkEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
