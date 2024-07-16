package am.devvibes.buyandsell.service.field;

import am.devvibes.buyandsell.dto.field.FieldDto;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;

import java.util.List;

public interface FieldService {

	FieldNameEntity addField(FieldDto fieldRequestDto);

	FieldNameEntity findFieldById(Long id);

	List<FieldNameEntity> findAllFields();

	void deleteFieldById(Long id);
}
