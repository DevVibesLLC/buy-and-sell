package am.devvibes.buyandsell.mapper.bus.busModel;

import am.devvibes.buyandsell.dto.vehicleModel.VehicleModelDto;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.bus.BusModelEntity;
import am.devvibes.buyandsell.mapper.auto.autoModel.AutoModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusModelMapperImpl implements BusModelMapper {

	@Override
	public VehicleModelDto mapEntityToDto(BusModelEntity autoModelEntity) {
		return VehicleModelDto.builder().id(autoModelEntity.getId()).model(autoModelEntity.getName()).build();
	}

	@Override
	public List<VehicleModelDto> mapEntityListToDtoList(List<BusModelEntity> autoModelEntityList) {
		return autoModelEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
