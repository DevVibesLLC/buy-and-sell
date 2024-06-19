package am.devvibes.buyandsell.mapper.bus.busMark;

import am.devvibes.buyandsell.dto.vehicle.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.entity.bus.BusMarkEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusMarkMapperImpl implements BusMarkMapper {

	@Override
	public VehicleMarkDto mapEntityToDto(BusMarkEntity busMarkEntity) {
		return VehicleMarkDto.builder()
				.id(busMarkEntity.getId())
				.mark(busMarkEntity.getName())
				.build();
	}

	@Override
	public List<VehicleMarkDto> mapEntityListToDtoList(List<BusMarkEntity> busMarkEntity) {
		return busMarkEntity.stream().map(this::mapEntityToDto).toList();
	}

}
