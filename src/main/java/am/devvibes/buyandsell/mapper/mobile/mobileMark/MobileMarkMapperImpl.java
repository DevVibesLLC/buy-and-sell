package am.devvibes.buyandsell.mapper.mobile.mobileMark;

import am.devvibes.buyandsell.dto.electronic.electronicMark.ElectronicMarkDto;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneMarkEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MobileMarkMapperImpl implements MobileMarkMapper{

	@Override
	public ElectronicMarkDto mapEntityToDto(MobilePhoneMarkEntity mobileMarkEntity) {
		return ElectronicMarkDto.builder()
				.id(mobileMarkEntity.getId())
				.mark(mobileMarkEntity.getName())
				.build();
	}

	@Override
	public List<ElectronicMarkDto> mapEntityListToDtoList(List<MobilePhoneMarkEntity> mobileMarkEntityList) {
		return mobileMarkEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
