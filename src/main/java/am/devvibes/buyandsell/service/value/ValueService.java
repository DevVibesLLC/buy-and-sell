package am.devvibes.buyandsell.service.value;

import am.devvibes.buyandsell.dto.value.FieldValuesDto;
import am.devvibes.buyandsell.entity.ValueEntity;

import java.util.List;

public interface ValueService {

	ValueEntity saveValue(FieldValuesDto fieldValuesDto);

	List<ValueEntity> saveAllValues(List<FieldValuesDto> fieldValuesDtos);

	ValueEntity findValueById(Long id);

	List<ValueEntity> findAllValues();

	void deleteValueById(Long id);

	List<ValueEntity> updateValues(List<ValueEntity> values,List<FieldValuesDto> fieldsValues);

}
