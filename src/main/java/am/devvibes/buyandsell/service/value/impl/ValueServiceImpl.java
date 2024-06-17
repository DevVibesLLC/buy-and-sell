package am.devvibes.buyandsell.service.value.impl;

import am.devvibes.buyandsell.dto.value.FieldValuesDto;
import am.devvibes.buyandsell.entity.field.FieldEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.value.ValueMapper;
import am.devvibes.buyandsell.repository.field.FieldRepository;
import am.devvibes.buyandsell.service.value.ValueService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class ValueServiceImpl implements ValueService {

	private final FieldRepository valueRepository;
	private final ValueMapper valueMapper;

	@Override
	@Transactional
	public FieldEntity saveValue(FieldValuesDto fieldValuesDto) {
		return valueRepository.save(valueMapper.mapDtoToEntity(fieldValuesDto));
	}

	@Override
	@Transactional
	public List<FieldEntity> saveAllValues(List<FieldValuesDto> fieldValuesDtos) {
		return fieldValuesDtos.stream().map(this::saveValue).toList();
	}

	@Override
	@Transactional
	public FieldEntity findValueById(Long id) {
		return valueRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.VALUE_NOT_FOUND));
	}

	@Override
	@Transactional
	public List<FieldEntity> findAllValues() {
		return valueRepository.findAll();
	}

	@Override
	@Transactional
	public void deleteValueById(Long id) {
		valueRepository.deleteById(id);
	}

	@Override
	@Transactional
	public List<FieldEntity> updateValues(List<FieldEntity> values, List<FieldValuesDto> fieldsValues) {
		if (isNull(fieldsValues) || fieldsValues.isEmpty())
			return values;

		Map<Long, FieldEntity> existingValuesMap = values.stream()
				.collect(Collectors.toMap(value -> value.getFieldName().getId(), value -> value));

		for (FieldValuesDto dto : fieldsValues) {
			FieldEntity valueEntity = existingValuesMap.get(dto.getFieldId());
			if (valueEntity != null) {
				valueEntity.setFieldValue(dto.getFieldValue());
			}
		}

		return valueRepository.saveAll(values);
	}


}
