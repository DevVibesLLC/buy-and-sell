package am.devvibes.buyandsell.service.category.impl;

import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.bus.BusMarkEntity;
import am.devvibes.buyandsell.entity.bus.BusModelEntity;
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
import am.devvibes.buyandsell.repository.mobile.MobilePhoneMarkRepository;
import am.devvibes.buyandsell.repository.truck.TruckMarkRepository;
import am.devvibes.buyandsell.service.category.ConstCategoryService;
import am.devvibes.buyandsell.util.CategoryEnum;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConstCategoryServiceImpl implements ConstCategoryService {

	private final CategoryRepository categoryRepository;
	private final AutoMarkRepository autoMarkRepository;
	private final TruckMarkRepository truckMarkRepository;
	private final BusMarkRepository busMarkRepository;
	private final MobilePhoneMarkRepository mobilePhoneMarkRepository;

	@Override
	@Transactional
	public List<AutoMarkEntity> findAutoMarks() {
		return categoryRepository.findByName(CategoryEnum.CARS)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND))
				.getAutoMarks();
	}

	@Override
	@Transactional
	public List<TruckMarkEntity> findTruckMarks() {
		return categoryRepository.findByName(CategoryEnum.TRUCKS)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND))
				.getTruckMarks();
	}

	@Override
	@Transactional
	public List<BusMarkEntity> findBusMarks() {
		return categoryRepository.findByName(CategoryEnum.BUSES)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND))
				.getBusMarks();
	}

	@Override
	public List<MotorcycleMarkEntity> findMotorcycleMarks() {
		return categoryRepository.findByName(CategoryEnum.MOTORCYCLES)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND))
				.getMotorcycleMarks();
	}

	@Override
	public List<MobilePhoneMarkEntity> findMobileMarks() {
		return categoryRepository.findByName(CategoryEnum.MOBILE_PHONES)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND))
				.getMobilePhoneMarks();
	}

	@Override
	public List<NotebookMarkEntity> findNotebookMarks() {
		return categoryRepository.findByName(CategoryEnum.NOTEBOOKS)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND))
				.getNotebookMarks();
	}

	@Override
	public List<AutoModelEntity> findAutoModelsByMark(Long markId) {
		return autoMarkRepository.findById(markId)
				.map(AutoMarkEntity::getModels)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MODEL_NOT_FOUND));
	}

	@Override
	public List<TruckModelEntity> findTruckModelsByMark(Long markId) {
		return truckMarkRepository.findById(markId)
				.map(TruckMarkEntity::getModels)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MODEL_NOT_FOUND));
	}

	@Override
	public List<BusModelEntity> findBusModelsByMark(Long markId) {
		return busMarkRepository.findById(markId)
				.map(BusMarkEntity::getModels)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MODEL_NOT_FOUND));
	}

	@Override
	public List<MobilePhoneModelEntity> findMobileModelsByMark(Long markId) {
		return mobilePhoneMarkRepository.findById(markId)
				.map(MobilePhoneMarkEntity::getModels)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MODEL_NOT_FOUND));
	}

}
