package am.devvibes.buyandsell.service.fieldName;

import am.devvibes.buyandsell.entity.FieldEntity;
import am.devvibes.buyandsell.entity.FieldNameEntity;

import java.util.List;

public interface FieldNameService {

	FieldNameEntity addFieldName(String name);

	FieldNameEntity findFieldNameById(Long id);

	List<FieldNameEntity> findAllFieldNames();

	void deleteFieldNameById(Long id);
}
