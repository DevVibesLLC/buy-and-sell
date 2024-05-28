package am.devvibes.buyandsell.mapper.impl;

import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.entity.FieldEntity;
import am.devvibes.buyandsell.mapper.FieldMapper;
import am.devvibes.buyandsell.service.fieldName.FieldNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FieldMapperImpl implements FieldMapper {

	private final FieldNameService fieldNameService;

	@Override
	public FieldEntity mapDtoToEntity(FieldRequestDto fieldRequestDto) {
		return  null;
	}

}
