package am.devvibes.buyandsell.service.constCategory;

import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.bus.BusMarkEntity;
import am.devvibes.buyandsell.entity.bus.BusModelEntity;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneMarkEntity;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneModelEntity;
import am.devvibes.buyandsell.entity.motorcycle.MotorcycleMarkEntity;
import am.devvibes.buyandsell.entity.notebook.NotebookMarkEntity;
import am.devvibes.buyandsell.entity.truck.TruckMarkEntity;
import am.devvibes.buyandsell.entity.truck.TruckModelEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.repository.auto.AutoMarkRepository;
import am.devvibes.buyandsell.repository.bus.BusMarkRepository;
import am.devvibes.buyandsell.repository.category.CategoryRepository;
import am.devvibes.buyandsell.repository.field.FieldNameRepository;
import am.devvibes.buyandsell.repository.mobile.MobilePhoneMarkRepository;
import am.devvibes.buyandsell.repository.truck.TruckMarkRepository;
import am.devvibes.buyandsell.service.category.impl.ConstCategoryServiceImpl;
import am.devvibes.buyandsell.util.CategoryEnum;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConstCategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private AutoMarkRepository autoMarkRepository;

	@Mock
	private TruckMarkRepository truckMarkRepository;

	@Mock
	private BusMarkRepository busMarkRepository;

	@Mock
	private MobilePhoneMarkRepository mobilePhoneMarkRepository;

	@Mock
	private FieldNameRepository fieldRepository;

	@InjectMocks
	private ConstCategoryServiceImpl constCategoryService;

	@Test
	void findAutoMarks__success() {

		CategoryEntity category = new CategoryEntity();
		category.setAutoMarks(Arrays.asList(new AutoMarkEntity(), new AutoMarkEntity()));

		when(categoryRepository.findByName(CategoryEnum.CARS)).thenReturn(Optional.of(category));

		List<AutoMarkEntity> autoMarks = constCategoryService.findAutoMarks();

		assertNotNull(autoMarks);
		assertEquals(2, autoMarks.size());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.CARS);
	}

	@Test
	void findAutoMarks__categoryNotFound() {

		when(categoryRepository.findByName(CategoryEnum.CARS)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> constCategoryService.findAutoMarks(), ExceptionConstants.CATEGORY_NOT_FOUND.getString());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.CARS);

	}

	@Test
	void findTruckMarks__success() {

		CategoryEntity category = new CategoryEntity();
		category.setTruckMarks(Arrays.asList(new TruckMarkEntity(), new TruckMarkEntity()));

		when(categoryRepository.findByName(CategoryEnum.TRUCKS)).thenReturn(Optional.of(category));

		List<TruckMarkEntity> truckMarks = constCategoryService.findTruckMarks();

		assertNotNull(truckMarks);
		assertEquals(2, truckMarks.size());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.TRUCKS);
	}

	@Test
	void findTruckMarks__categoryNotFound() {

		when(categoryRepository.findByName(CategoryEnum.TRUCKS)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> constCategoryService.findTruckMarks(), ExceptionConstants.CATEGORY_NOT_FOUND.getString());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.TRUCKS);

	}

	@Test
	void findBusMarks__success() {

		CategoryEntity category = new CategoryEntity();
		category.setBusMarks(Arrays.asList(new BusMarkEntity(), new BusMarkEntity()));

		when(categoryRepository.findByName(CategoryEnum.BUSES)).thenReturn(Optional.of(category));

		List<BusMarkEntity> busMarks = constCategoryService.findBusMarks();

		assertNotNull(busMarks);
		assertEquals(2, busMarks.size());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.BUSES);

	}

	@Test
	void findBusMarks__categoryNotFound() {

		when(categoryRepository.findByName(CategoryEnum.BUSES)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> constCategoryService.findBusMarks(), ExceptionConstants.CATEGORY_NOT_FOUND.getString());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.BUSES);

	}

	@Test
	void findMotorcycleMarks__success() {

		CategoryEntity category = new CategoryEntity();
		category.setMotorcycleMarks(Arrays.asList(new MotorcycleMarkEntity(), new MotorcycleMarkEntity()));

		when(categoryRepository.findByName(CategoryEnum.MOTORCYCLES)).thenReturn(Optional.of(category));

		List<MotorcycleMarkEntity> motorcycleMarks = constCategoryService.findMotorcycleMarks();

		assertNotNull(motorcycleMarks);
		assertEquals(2, motorcycleMarks.size());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.MOTORCYCLES);
	}

	@Test
	void findMotorcycleMarks__categoryNotFound() {

		when(categoryRepository.findByName(CategoryEnum.MOTORCYCLES)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> constCategoryService.findMotorcycleMarks(), ExceptionConstants.CATEGORY_NOT_FOUND.getString());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.MOTORCYCLES);

	}

	@Test
	void findMobileMarks__success() {

		CategoryEntity category = new CategoryEntity();
		category.setMobilePhoneMarks(Arrays.asList(new MobilePhoneMarkEntity(), new MobilePhoneMarkEntity()));

		when(categoryRepository.findByName(CategoryEnum.MOBILE_PHONES)).thenReturn(Optional.of(category));

		List<MobilePhoneMarkEntity> mobileMarks = constCategoryService.findMobileMarks();

		assertNotNull(mobileMarks);
		assertEquals(2, mobileMarks.size());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.MOBILE_PHONES);

	}

	@Test
	void findMobileMarks__categoryNotFound() {

		when(categoryRepository.findByName(CategoryEnum.MOBILE_PHONES)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> constCategoryService.findMobileMarks(), ExceptionConstants.CATEGORY_NOT_FOUND.getString());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.MOBILE_PHONES);

	}

	@Test
	void findNotebookMarks__success() {

		CategoryEntity category = new CategoryEntity();
		category.setNotebookMarks(Arrays.asList(new NotebookMarkEntity(), new NotebookMarkEntity()));

		when(categoryRepository.findByName(CategoryEnum.NOTEBOOKS)).thenReturn(Optional.of(category));

		List<NotebookMarkEntity> notebookMarks = constCategoryService.findNotebookMarks();

		assertNotNull(notebookMarks);
		assertEquals(2, notebookMarks.size());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.NOTEBOOKS);
	}

	@Test
	void findNotebookMarks__categoryNotFound() {

		when(categoryRepository.findByName(CategoryEnum.NOTEBOOKS)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> constCategoryService.findNotebookMarks(), ExceptionConstants.CATEGORY_NOT_FOUND.getString());

		verify(categoryRepository, times(1)).findByName(CategoryEnum.NOTEBOOKS);

	}

	@Test
	void findAutoModelsByMark__success() {

		Long markId = 1L;
		AutoMarkEntity autoMarkEntity = new AutoMarkEntity();
		autoMarkEntity.setModels(Arrays.asList(new AutoModelEntity(), new AutoModelEntity()));

		when(autoMarkRepository.findById(markId)).thenReturn(Optional.of(autoMarkEntity));

		List<AutoModelEntity> autoModels = constCategoryService.findAutoModelsByMark(markId);

		assertNotNull(autoModels);
		assertEquals(2, autoModels.size());

		verify(autoMarkRepository, times(1)).findById(markId);
	}

	@Test
	void findAutoModelsByMark__modelNotFound() {

		Long markId = 1L;
		when(autoMarkRepository.findById(markId)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> constCategoryService.findAutoModelsByMark(markId), ExceptionConstants.MODEL_NOT_FOUND.getString());

		verify(autoMarkRepository, times(1)).findById(markId);

	}

	@Test
	void findTruckModelsByMark__success() {

		Long markId = 1L;
		TruckMarkEntity truckMarkEntity = new TruckMarkEntity();
		truckMarkEntity.setModels(Arrays.asList(new TruckModelEntity(), new TruckModelEntity()));

		when(truckMarkRepository.findById(markId)).thenReturn(Optional.of(truckMarkEntity));

		List<TruckModelEntity> truckModels = constCategoryService.findTruckModelsByMark(markId);

		assertNotNull(truckModels);
		assertEquals(2, truckModels.size());

		verify(truckMarkRepository, times(1)).findById(markId);
	}

	@Test
	void findTruckModelsByMark__modelNotFound() {

		Long markId = 1L;
		when(truckMarkRepository.findById(markId)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> constCategoryService.findTruckModelsByMark(markId), ExceptionConstants.MODEL_NOT_FOUND.getString());

		verify(truckMarkRepository, times(1)).findById(markId);

	}

	@Test
	void findBusModelsByMark__success() {

		Long markId = 1L;
		BusMarkEntity busMarkEntity = new BusMarkEntity();
		busMarkEntity.setModels(Arrays.asList(new BusModelEntity(), new BusModelEntity()));

		when(busMarkRepository.findById(markId)).thenReturn(Optional.of(busMarkEntity));

		List<BusModelEntity> busModels = constCategoryService.findBusModelsByMark(markId);

		assertNotNull(busModels);
		assertEquals(2, busModels.size());

		verify(busMarkRepository, times(1)).findById(markId);
	}

	@Test
	void findBusModelsByMark__modelNotFound() {

		Long markId = 1L;
		when(busMarkRepository.findById(markId)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> constCategoryService.findBusModelsByMark(markId), ExceptionConstants.MODEL_NOT_FOUND.getString());

		verify(busMarkRepository, times(1)).findById(markId);

	}

	@Test
	void findMobileModelsByMark__success() {

		Long markId = 1L;
		MobilePhoneMarkEntity mobileMarkEntity = new MobilePhoneMarkEntity();
		mobileMarkEntity.setModels(Arrays.asList(new MobilePhoneModelEntity(), new MobilePhoneModelEntity()));

		when(mobilePhoneMarkRepository.findById(markId)).thenReturn(Optional.of(mobileMarkEntity));

		List<MobilePhoneModelEntity> mobileModels = constCategoryService.findMobileModelsByMark(markId);

		assertNotNull(mobileModels);
		assertEquals(2, mobileModels.size());

		verify(mobilePhoneMarkRepository, times(1)).findById(markId);
	}

	@Test
	void findMobileModelsByMark__modelNotFound() {

		Long markId = 1L;
		when(mobilePhoneMarkRepository.findById(markId)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> constCategoryService.findMobileModelsByMark(markId), ExceptionConstants.MODEL_NOT_FOUND.getString());

		verify(mobilePhoneMarkRepository, times(1)).findById(markId);

	}

}
