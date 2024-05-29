package am.devvibes.buyandsell.mapper.autoMark;

import am.devvibes.buyandsell.dto.autoMark.AutoMarkDto;
import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;

import java.util.List;

public interface AutoMarkMapper {

	AutoMarkDto mapEntityToDto(AutoMarkEntity autoMarkEntity);

	List<AutoMarkDto> mapEntityListToDtoList(List<AutoMarkEntity> autoMarkEntityList);

}
