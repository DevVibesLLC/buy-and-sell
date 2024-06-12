package am.devvibes.buyandsell.mapper.auto.autoMark;

import am.devvibes.buyandsell.dto.autoMark.VehicleMarkDto;
import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutoMarkMapperImpl implements AutoMarkMapper {

	@Override
	public VehicleMarkDto mapEntityToDto(AutoMarkEntity autoMarkEntity) {
		return VehicleMarkDto.builder()
				.id(autoMarkEntity.getId())
				.mark(autoMarkEntity.getName())
				.build();
	}

	@Override
	public List<VehicleMarkDto> mapEntityListToDtoList(List<AutoMarkEntity> autoMarkEntityList) {
		return autoMarkEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
