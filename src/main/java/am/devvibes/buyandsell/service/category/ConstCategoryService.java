package am.devvibes.buyandsell.service.category;

import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.auto.GenerationEntity;

import java.util.List;

public interface ConstCategoryService {

	List<AutoMarkEntity> findMarksByCategory(Long categoryId);

	List<AutoModelEntity> findModelsByMark(Long markId);

	List<GenerationEntity> findGenerationsByModel(Long modelId);

}
