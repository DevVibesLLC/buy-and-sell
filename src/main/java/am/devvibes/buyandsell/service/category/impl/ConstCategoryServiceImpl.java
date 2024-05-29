package am.devvibes.buyandsell.service.category.impl;

import am.devvibes.buyandsell.dto.autoMark.AutoMarkDto;
import am.devvibes.buyandsell.dto.autoModel.AutoModelDto;
import am.devvibes.buyandsell.dto.generation.GenerationDto;
import am.devvibes.buyandsell.entity.CategoryEntity;
import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.auto.GenerationEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.autoMark.AutoMarkMapper;
import am.devvibes.buyandsell.mapper.autoModel.AutoModelMapper;
import am.devvibes.buyandsell.mapper.generation.GenerationMapper;
import am.devvibes.buyandsell.repository.AutoMarkRepository;
import am.devvibes.buyandsell.repository.AutoModelRepository;
import am.devvibes.buyandsell.repository.CategoryRepository;
import am.devvibes.buyandsell.service.category.ConstCategoryService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConstCategoryServiceImpl implements ConstCategoryService {

	private final CategoryRepository categoryRepository;
	private final AutoMarkRepository markRepository;
	private final AutoModelRepository modelRepository;
	private final AutoMarkMapper autoMarkMapper;
	private final AutoModelMapper autoModelMapper;
	private final GenerationMapper generationMapper;

	@Override
	@Transactional
	public List<AutoMarkDto> findMarksByCategory(Long categoryId) {
		List<AutoMarkEntity> autoMarkEntities = categoryRepository.findById(categoryId)
				.map(CategoryEntity::getMarks)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MARK_NOT_FOUND));
		return autoMarkMapper.mapEntityListToDtoList(autoMarkEntities);
	}

	@Override
	@Transactional
	public List<AutoModelDto> findModelsByMark(Long markId) {
		List<AutoModelEntity> autoModelEntities = markRepository.findById(markId)
				.map(AutoMarkEntity::getModels)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.MODEL_NOT_FOUND));
		return autoModelMapper.mapEntityListToDtoList(autoModelEntities);
	}

	@Override
	@Transactional
	public List<GenerationDto> findGenerationsByModel(Long modelId) {
		List<GenerationEntity> generationEntities = modelRepository.findById(modelId)
				.map(AutoModelEntity::getGenerations)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.GENERATION_NOT_FOUND));
		return generationMapper.mapEntityListToDtoList(generationEntities);
	}

}
