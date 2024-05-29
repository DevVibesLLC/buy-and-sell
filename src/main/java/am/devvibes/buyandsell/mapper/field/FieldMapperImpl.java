package am.devvibes.buyandsell.mapper.field;

import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.entity.FieldEntity;
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
