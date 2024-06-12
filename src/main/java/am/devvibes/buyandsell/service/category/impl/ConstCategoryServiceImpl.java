package am.devvibes.buyandsell.service.category.impl;

import am.devvibes.buyandsell.dto.autoMark.VehicleMarkDto;
import am.devvibes.buyandsell.dto.autoModel.VehicleModelDto;
import am.devvibes.buyandsell.entity.CategoryEntity;
import am.devvibes.buyandsell.entity.FieldNameEntity;
import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.truck.TruckMarkEntity;
import am.devvibes.buyandsell.entity.truck.TruckModelEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.auto.autoMark.AutoMarkMapper;
import am.devvibes.buyandsell.mapper.auto.autoModel.AutoModelMapper;
import am.devvibes.buyandsell.mapper.truck.truckMark.TruckMarkMapper;
import am.devvibes.buyandsell.mapper.truck.truckModel.TruckModelMapper;
import am.devvibes.buyandsell.repository.auto.AutoMarkRepository;
import am.devvibes.buyandsell.repository.category.CategoryRepository;
import am.devvibes.buyandsell.repository.field.FieldNameRepository;
import am.devvibes.buyandsell.repository.truck.TruckMarkRepository;
import am.devvibes.buyandsell.service.category.ConstCategoryService;
import am.devvibes.buyandsell.util.CategoryEnum;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class ConstCategoryServiceImpl implements ConstCategoryService {

	private final CategoryRepository categoryRepository;
	private final AutoMarkRepository autoMarkRepository;
	private final TruckMarkRepository truckMarkRepository;
	private final FieldNameRepository fieldRepository;
	private final AutoMarkMapper autoMarkMapper;
	private final AutoModelMapper autoModelMapper;
	private final TruckMarkMapper truckMarkMapper;
	private final TruckModelMapper truckModelMapper;

	@Override
	@Transactional
	public List<VehicleMarkDto> findMarksByCategory(Long categoryId) {
		CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND));

		switch (categoryEntity.getName()){
			case CategoryEnum.CAR -> {
				List<AutoMarkEntity> autoMarks = categoryEntity.getAutoMarks();
				return autoMarkMapper.mapEntityListToDtoList(autoMarks);
			}
			case CategoryEnum.TRUCK -> {
				List<TruckMarkEntity> truckMarks = categoryEntity.getTruckMarks();
				return truckMarkMapper.mapEntityListToDtoList(truckMarks);
			}
			default -> throw new NotFoundException(ExceptionConstants.CATEGORY_NOT_FOUND);
		}
	}

	@Override
	public List<VehicleModelDto> findAutoModelsByMark(Long markId) {
		List<AutoModelEntity> autoModelEntities = autoMarkRepository.findById(markId)
				.map(AutoMarkEntity::getModels)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MODEL_NOT_FOUND));
		return autoModelMapper.mapEntityListToDtoList(autoModelEntities);
	}

	@Override
	public List<VehicleMarkDto> findTruckMarksByCategory(Long categoryId) {
		List<TruckMarkEntity> truckMarkEntities = categoryRepository.findById(categoryId)
				.map(CategoryEntity::getTruckMarks)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MARK_NOT_FOUND));
		return truckMarkMapper.mapEntityListToDtoList(truckMarkEntities);
	}

	@Override
	public List<VehicleModelDto> findTruckModelsByMark(Long markId) {
		List<TruckModelEntity> truckModelEntities = truckMarkRepository.findById(markId)
				.map(TruckMarkEntity::getModels)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MODEL_NOT_FOUND));
		return truckModelMapper.mapEntityListToDtoList(truckModelEntities);
	}

	@Override
	public List<String> findByFieldNameId(Long id) {
		FieldNameEntity fieldNameEntity = fieldRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.FIELD_NAME_NOT_FOUND));
		return fieldNameEntity.getValue();
	}

}
