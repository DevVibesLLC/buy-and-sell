package am.devvibes.buyandsell.service.category.impl;

import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.auto.GenerationEntity;
import am.devvibes.buyandsell.repository.CategoryRepository;
import am.devvibes.buyandsell.repository.AutoMarkRepository;
import am.devvibes.buyandsell.repository.AutoModelRepository;
import am.devvibes.buyandsell.service.category.ConstCategoryService;
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

	@Override
	@Transactional
	public List<AutoMarkEntity> findMarksByCategory(Long categoryId) {
		return categoryRepository.findMarksById(categoryId);
	}

	@Override
	@Transactional
	public List<AutoModelEntity> findModelsByMark(Long markId) {
		return markRepository.findModelsById(markId);
	}

	@Override
	@Transactional
	public List<GenerationEntity> findGenerationsByModel(Long modelId) {
		return modelRepository.findGenerationsById(modelId);
	}

}
