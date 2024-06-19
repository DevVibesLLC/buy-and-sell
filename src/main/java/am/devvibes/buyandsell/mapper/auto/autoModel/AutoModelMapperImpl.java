package am.devvibes.buyandsell.mapper.auto.autoModel;

import am.devvibes.buyandsell.dto.vehicle.vehicleModel.VehicleModelDto;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutoModelMapperImpl implements AutoModelMapper {

	@Override
	public VehicleModelDto mapEntityToDto(AutoModelEntity autoModelEntity) {
		return VehicleModelDto.builder().id(autoModelEntity.getId()).model(autoModelEntity.getName()).build();
	}

	@Override
	public List<VehicleModelDto> mapEntityListToDtoList(List<AutoModelEntity> autoModelEntityList) {
		return autoModelEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
