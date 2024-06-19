package am.devvibes.buyandsell.mapper.mobile.mobileModel;

import am.devvibes.buyandsell.dto.electronic.electronicModel.ElectronicModelDto;
import am.devvibes.buyandsell.dto.vehicle.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.entity.bus.BusMarkEntity;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneModelEntity;

import java.util.List;

public interface MobileModelMapper {

	ElectronicModelDto mapEntityToDto(MobilePhoneModelEntity mobileModelEntity);

	List<ElectronicModelDto> mapEntityListToDtoList(List<MobilePhoneModelEntity> mobileModelEntityList);

}
