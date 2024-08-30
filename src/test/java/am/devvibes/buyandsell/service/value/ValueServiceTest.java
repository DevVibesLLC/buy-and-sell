package am.devvibes.buyandsell.service.value;

import am.devvibes.buyandsell.dto.field.FieldValueDto;
import am.devvibes.buyandsell.dto.value.FieldValuesDto;
import am.devvibes.buyandsell.entity.field.FieldEntity;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;
import am.devvibes.buyandsell.entity.field.FieldValueEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.value.ValueMapper;
import am.devvibes.buyandsell.repository.field.FieldRepository;
import am.devvibes.buyandsell.service.value.impl.ValueServiceImpl;
import am.devvibes.buyandsell.util.ExceptionConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValueServiceTest {

	@Mock
	private FieldRepository fieldRepository;

	@Mock
	private ValueMapper valueMapper;

	@InjectMocks
	private ValueServiceImpl valueService;

	@Test
	void saveValue_success() {

		FieldValuesDto fieldValuesDto = new FieldValuesDto();
		FieldEntity fieldEntity = new FieldEntity();

		when(valueMapper.mapDtoToEntity(fieldValuesDto)).thenReturn(fieldEntity);
		when(fieldRepository.save(any(FieldEntity.class))).thenReturn(fieldEntity);

		FieldEntity result = valueService.saveValue(fieldValuesDto);

		assertNotNull(result);
		assertEquals(result, fieldEntity);

		verify(valueMapper, times(1)).mapDtoToEntity(fieldValuesDto);
		verify(fieldRepository, times(1)).save(fieldEntity);
	}

	@Test
	public void testSaveAllValues() {

		FieldValuesDto dto1 = new FieldValuesDto();
		FieldValuesDto dto2 = new FieldValuesDto();

		FieldEntity entity1 = new FieldEntity();
		FieldEntity entity2 = new FieldEntity();

		List<FieldValuesDto> dtos = List.of(dto1, dto2);
		List<FieldEntity> entities = List.of(entity1, entity2);

		when(valueMapper.mapDtoToEntity(dto1)).thenReturn(entity1);
		when(valueMapper.mapDtoToEntity(dto2)).thenReturn(entity2);
		when(fieldRepository.save(entity1)).thenReturn(entity1);
		when(fieldRepository.save(entity2)).thenReturn(entity2);

		List<FieldEntity> result = valueService.saveAllValues(dtos);

		assertNotNull(result);
		assertEquals(entities, result);

		verify(valueMapper, times(dtos.size())).mapDtoToEntity(any(FieldValuesDto.class));
		verify(fieldRepository, times(dtos.size())).save(any(FieldEntity.class));
	}

	@Test
	public void findValueById__success() {

		FieldEntity expectedEntity = new FieldEntity();
		expectedEntity.setId(1L);

		when(fieldRepository.findById(expectedEntity.getId())).thenReturn(Optional.of(expectedEntity));

		FieldEntity result = valueService.findValueById(expectedEntity.getId());

		assertNotNull(result);
		assertEquals(expectedEntity, result);

		verify(fieldRepository, times(1)).findById(expectedEntity.getId());
	}

	@Test
	public void findValueById__valueNotFound() {

		Long id = 1L;

		when(fieldRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> valueService.findValueById(id), ExceptionConstants.FIELD_NOT_FOUND.getString());

		verify(fieldRepository, times(1)).findById(id);
	}

	@Test
	public void findAllValues__success() {

		FieldEntity entity1 = new FieldEntity();
		entity1.setId(1L);

		FieldEntity entity2 = new FieldEntity();
		entity2.setId(2L);

		List<FieldEntity> expectedEntities = Arrays.asList(entity1, entity2);

		when(fieldRepository.findAll()).thenReturn(expectedEntities);

		List<FieldEntity> result = valueService.findAllValues();

		assertEquals(result.size(), expectedEntities.size());
		assertEquals(expectedEntities, result);

		verify(fieldRepository, times(1)).findAll();
	}

	@Test
	public void deleteValueById__success() {

		Long id = 1L;

		when(fieldRepository.existsById(id)).thenReturn(true);

		valueService.deleteValueById(id);

		verify(fieldRepository, times(1)).deleteById(id);
	}

	@Test
	public void deleteValueById__fieldNotFound() {

		Long id = 1L;

		when(fieldRepository.existsById(id)).thenReturn(false);

		assertThrows(NotFoundException.class, () -> valueService.deleteValueById(id), ExceptionConstants.FIELD_NOT_FOUND.getString());

		verify(fieldRepository, times(0)).deleteById(id);
	}

	@Test
	public void updateValues__success() {

		FieldNameEntity fieldName1 = new FieldNameEntity();
		fieldName1.setId(1L);

		FieldNameEntity fieldName2 = new FieldNameEntity();
		fieldName2.setId(2L);

		FieldEntity existingEntity1 = new FieldEntity();
		existingEntity1.setFieldName(fieldName1);
		existingEntity1.setFieldValue_eng("value1");
		existingEntity1.setFieldValue_ru("значение1");
		existingEntity1.setFieldValue_hy("արժեք1");

		FieldEntity existingEntity2 = new FieldEntity();
		existingEntity2.setFieldName(fieldName2);
		existingEntity2.setFieldValue_eng("value2");
		existingEntity2.setFieldValue_ru("значениение2");
		existingEntity2.setFieldValue_hy("արժեք2");

		List<FieldEntity> existingValues = Arrays.asList(existingEntity1, existingEntity2);

		FieldValueEntity newFieldValue1 = new FieldValueEntity();
		newFieldValue1.setValue_eng("newValue1");
		newFieldValue1.setValue_ru("новоеЗначение1");
		newFieldValue1.setValue_hy("նորԱրժեք1");

		FieldValueDto fieldValueDto1 = FieldValueDto.builder()
				.fieldValue_eng("newValue1")
				.fieldValue_ru("новоеЗначение1")
				.fieldValue_hy("նորԱրժեք1")
				.build();

		FieldValueDto fieldValueDto2 = FieldValueDto.builder()
				.fieldValue_eng("newValue2")
				.fieldValue_ru("новоеЗначение2")
				.fieldValue_hy("նորԱրժեք2")
				.build();

		FieldValuesDto dto1 = new FieldValuesDto();
		dto1.setFieldId(1L);
		dto1.setFieldValues(fieldValueDto1);

		FieldValuesDto dto2 = new FieldValuesDto();
		dto2.setFieldId(2L);
		dto2.setFieldValues(fieldValueDto2);

		List<FieldValuesDto> dtos = List.of(dto1, dto2);

		when(fieldRepository.saveAll(existingValues)).thenReturn(existingValues);

		List<FieldEntity> updatedEntities = valueService.updateValues(existingValues, dtos);

		assertEquals("newValue1", updatedEntities.get(0).getFieldValue_eng());
		assertEquals("новоеЗначение1", updatedEntities.get(0).getFieldValue_ru());
		assertEquals("նորԱրժեք1", updatedEntities.get(0).getFieldValue_hy());

		assertEquals("newValue2", updatedEntities.get(1).getFieldValue_eng());
		assertEquals("новоеЗначение2", updatedEntities.get(1).getFieldValue_ru());
		assertEquals("նորԱրժեք2", updatedEntities.get(1).getFieldValue_hy());

		verify(fieldRepository, times(1)).saveAll(existingValues);
	}

	@Test
	public void updateValues__inputIsNull() {

		List<FieldEntity> fieldEntities = valueService.updateValues(null, List.of());

		assertNull(fieldEntities);

		verify(fieldRepository, times(0)).saveAll(anyList());

	}

	@Test
	public void updateValues__inputIsEmpty() {

		List<FieldEntity> fieldEntities = valueService.updateValues(List.of(), List.of());

		assertTrue(fieldEntities.isEmpty());

		verify(fieldRepository, times(0)).saveAll(anyList());

	}


}
