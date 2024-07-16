package am.devvibes.buyandsell.mapper.field;

import am.devvibes.buyandsell.dto.field.FieldDto;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FieldMapperImpl implements FieldMapper {

	@Override
	public FieldNameEntity mapDtoToEntity(FieldDto fieldRequestDto) {
		return  null;
	}

}
