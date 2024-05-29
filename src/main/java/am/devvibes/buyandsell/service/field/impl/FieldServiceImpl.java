package am.devvibes.buyandsell.service.field.impl;

import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.entity.FieldEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.field.FieldMapper;
import am.devvibes.buyandsell.repository.FieldRepository;
import am.devvibes.buyandsell.service.field.FieldService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldServiceImpl implements FieldService {

	private final FieldRepository fieldRepository;
	private final FieldMapper fieldMapper;

	@Override
	@Transactional
	public FieldEntity addField(FieldRequestDto fieldRequestDto) {
		return fieldRepository.save(fieldMapper.mapDtoToEntity(fieldRequestDto));
	}

	@Override
	@Transactional
	public FieldEntity findFieldById(Long id) {
		return fieldRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.FIELD_NOT_FOUND));
	}

	@Override
	@Transactional
	public List<FieldEntity> findAllFields() {
		return fieldRepository.findAll();
	}

	@Override
	@Transactional
	public void deleteFieldById(Long id) {
		fieldRepository.deleteById(id);
	}

}
