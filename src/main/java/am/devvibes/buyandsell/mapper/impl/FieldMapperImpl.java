package am.devvibes.buyandsell.mapper.impl;

import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.entity.FieldEntity;
import am.devvibes.buyandsell.mapper.FieldMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FieldMapperImpl implements FieldMapper {

	@Override
	public FieldEntity mapDtoToEntity(FieldRequestDto fieldRequestDto) {
		return  null;
	}

}
