package am.devvibes.buyandsell.service.category.impl;

import am.devvibes.buyandsell.dto.electronic.electronicMark.ElectronicMarkDto;
import am.devvibes.buyandsell.dto.electronic.electronicModel.ElectronicModelDto;
import am.devvibes.buyandsell.dto.vehicle.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.dto.vehicle.vehicleModel.VehicleModelDto;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;
import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.bus.BusMarkEntity;
import am.devvibes.buyandsell.entity.bus.BusModelEntity;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneMarkEntity;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneModelEntity;
import am.devvibes.buyandsell.entity.truck.TruckMarkEntity;
import am.devvibes.buyandsell.entity.truck.TruckModelEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.auto.autoMark.AutoMarkMapper;
import am.devvibes.buyandsell.mapper.auto.autoModel.AutoModelMapper;
import am.devvibes.buyandsell.mapper.bus.busMark.BusMarkMapper;
import am.devvibes.buyandsell.mapper.bus.busModel.BusModelMapper;
import am.devvibes.buyandsell.mapper.mobile.mobileMark.MobileMarkMapper;
import am.devvibes.buyandsell.mapper.mobile.mobileModel.MobileModelMapper;
import am.devvibes.buyandsell.mapper.truck.truckMark.TruckMarkMapper;
import am.devvibes.buyandsell.mapper.truck.truckModel.TruckModelMapper;
import am.devvibes.buyandsell.repository.auto.AutoMarkRepository;
import am.devvibes.buyandsell.repository.bus.BusMarkRepository;
import am.devvibes.buyandsell.repository.category.CategoryRepository;
import am.devvibes.buyandsell.repository.field.FieldNameRepository;
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
	private final FieldNameRepository fieldRepository;
	private final AutoMarkMapper autoMarkMapper;
	private final AutoModelMapper autoModelMapper;
	private final TruckMarkMapper truckMarkMapper;
	private final TruckModelMapper truckModelMapper;
	private final BusMarkMapper busMarkMapper;
	private final BusModelMapper busModelMapper;
	private final MobileMarkMapper mobileMarkMapper;
	private final MobileModelMapper mobileModelMapper;

	@Override
	@Transactional
	public List<VehicleMarkDto> findAutoMarks() {
		CategoryEntity categoryEntity = categoryRepository.findByName(CategoryEnum.CAR)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND));

		return autoMarkMapper.mapEntityListToDtoList(categoryEntity.getAutoMarks());
	}

	@Override
	@Transactional
	public List<VehicleMarkDto> findTruckMarks() {
		CategoryEntity categoryEntity = categoryRepository.findByName(CategoryEnum.TRUCK)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND));

		return truckMarkMapper.mapEntityListToDtoList(categoryEntity.getTruckMarks());
	}

	@Override
	@Transactional
	public List<VehicleMarkDto> findBusMarks() {
		CategoryEntity categoryEntity = categoryRepository.findByName(CategoryEnum.BUS)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND));

		return busMarkMapper.mapEntityListToDtoList(categoryEntity.getBusMarks());
	}

	@Override
	public List<ElectronicMarkDto> findMobileMarks() {
		CategoryEntity categoryEntity = categoryRepository.findByName(CategoryEnum.MOBILE_PHONE)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND));

		return mobileMarkMapper.mapEntityListToDtoList(categoryEntity.getMobilePhoneMarks());
	}

	@Override
	public List<ElectronicMarkDto> findNotebookMarks() {
		CategoryEntity categoryEntity = categoryRepository.findByName(CategoryEnum.NOTEBOOK)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND));

		return mobileMarkMapper.mapEntityListToDtoList(categoryEntity.getMobilePhoneMarks());
	}

	@Override
	public List<VehicleModelDto> findAutoModelsByMark(Long markId) {
		List<AutoModelEntity> autoModelEntities = autoMarkRepository.findById(markId)
				.map(AutoMarkEntity::getModels)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MODEL_NOT_FOUND));
		return autoModelMapper.mapEntityListToDtoList(autoModelEntities);
	}


	@Override
	public List<VehicleModelDto> findTruckModelsByMark(Long markId) {
		List<TruckModelEntity> truckModelEntities = truckMarkRepository.findById(markId)
				.map(TruckMarkEntity::getModels)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MODEL_NOT_FOUND));
		return truckModelMapper.mapEntityListToDtoList(truckModelEntities);
	}

	@Override
	public List<VehicleModelDto> findBusModelsByMark(Long markId) {
		List<BusModelEntity> busModelEntities = busMarkRepository.findById(markId)
				.map(BusMarkEntity::getModels)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MODEL_NOT_FOUND));
		return busModelMapper.mapEntityListToDtoList(busModelEntities);
	}

	@Override
	public List<ElectronicModelDto> findMobileModelsByMark(Long markId) {
		List<MobilePhoneModelEntity> mobilePhoneModelEntities = mobilePhoneMarkRepository.findById(markId)
				.map(MobilePhoneMarkEntity::getModels)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MODEL_NOT_FOUND));

		return mobileModelMapper.mapEntityListToDtoList(mobilePhoneModelEntities);
	}

	@Override
	public List<String> findByFieldNameId(Long id) {
		FieldNameEntity fieldNameEntity = fieldRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.FIELD_NAME_NOT_FOUND));
		return fieldNameEntity.getValue();
	}

}
