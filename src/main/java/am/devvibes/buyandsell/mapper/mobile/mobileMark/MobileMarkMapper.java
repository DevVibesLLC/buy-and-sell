package am.devvibes.buyandsell.mapper.mobile.mobileMark;

import am.devvibes.buyandsell.dto.electronic.electronicMark.ElectronicMarkDto;
import am.devvibes.buyandsell.dto.vehicle.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.entity.bus.BusMarkEntity;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneMarkEntity;

import java.util.List;

public interface MobileMarkMapper {

	ElectronicMarkDto mapEntityToDto(MobilePhoneMarkEntity mobileMarkEntity);

	List<ElectronicMarkDto> mapEntityListToDtoList(List<MobilePhoneMarkEntity> mobileMarkEntityList);

}
