package am.devvibes.buyandsell.service.value;

import am.devvibes.buyandsell.dto.value.FieldValuesDto;
import am.devvibes.buyandsell.entity.FieldEntity;

import java.util.List;

public interface ValueService {

	FieldEntity saveValue(FieldValuesDto fieldValuesDto);

	List<FieldEntity> saveAllValues(List<FieldValuesDto> fieldValuesDtos);

	FieldEntity findValueById(Long id);

	List<FieldEntity> findAllValues();

	void deleteValueById(Long id);

	List<FieldEntity> updateValues(List<FieldEntity> values,List<FieldValuesDto> fieldsValues);

}
