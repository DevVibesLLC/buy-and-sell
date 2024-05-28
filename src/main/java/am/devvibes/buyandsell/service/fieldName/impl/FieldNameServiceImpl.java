package am.devvibes.buyandsell.service.fieldName.impl;

import am.devvibes.buyandsell.entity.FieldNameEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.repository.FieldNameRepository;
import am.devvibes.buyandsell.service.fieldName.FieldNameService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldNameServiceImpl implements FieldNameService {

	private final FieldNameRepository fieldNameRepository;

	@Override
	@Transactional
	public FieldNameEntity addFieldName(String name) {
		return fieldNameRepository.save(FieldNameEntity.builder().name(name).build());
	}

	@Override
	@Transactional
	public FieldNameEntity findFieldNameById(Long id) {
		return fieldNameRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.FIELD_NAME_NOT_FOUND));
	}

	@Override
	@Transactional
	public List<FieldNameEntity> findAllFieldNames() {
		return fieldNameRepository.findAll();
	}

	@Override
	@Transactional
	public void deleteFieldNameById(Long id) {
		fieldNameRepository.deleteById(id);
	}

}
