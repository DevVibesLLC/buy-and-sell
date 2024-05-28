package am.devvibes.buyandsell.service.field;

import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.entity.FieldEntity;

import java.util.List;

public interface FieldService {

	FieldEntity addField(FieldRequestDto fieldRequestDto);

	FieldEntity findFieldById(Long id);

	List<FieldEntity> findAllFields();

	void deleteFieldById(Long id);
}
