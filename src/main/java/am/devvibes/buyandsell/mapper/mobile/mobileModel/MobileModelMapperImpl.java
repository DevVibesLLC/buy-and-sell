package am.devvibes.buyandsell.mapper.mobile.mobileModel;

import am.devvibes.buyandsell.dto.electronic.electronicModel.ElectronicModelDto;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneModelEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MobileModelMapperImpl implements MobileModelMapper{

	@Override
	public ElectronicModelDto mapEntityToDto(MobilePhoneModelEntity mobileModelEntity) {
		return ElectronicModelDto.builder()
				.id(mobileModelEntity.getId())
				.model(mobileModelEntity.getName())
				.build();
	}

	@Override
	public List<ElectronicModelDto> mapEntityListToDtoList(List<MobilePhoneModelEntity> mobileModelEntityList) {
		return mobileModelEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
