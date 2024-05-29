package am.devvibes.buyandsell.mapper.autoMark;

import am.devvibes.buyandsell.dto.autoMark.AutoMarkDto;
import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutoMarkMapperImpl implements AutoMarkMapper {

	@Override
	public AutoMarkDto mapEntityToDto(AutoMarkEntity autoMarkEntity) {
		return AutoMarkDto.builder()
				.id(autoMarkEntity.getId())
				.mark(autoMarkEntity.getName())
				.build();
	}

	@Override
	public List<AutoMarkDto> mapEntityListToDtoList(List<AutoMarkEntity> autoMarkEntityList) {
		return autoMarkEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
