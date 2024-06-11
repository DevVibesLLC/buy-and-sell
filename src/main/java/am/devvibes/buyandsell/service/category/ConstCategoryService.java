package am.devvibes.buyandsell.service.category;

import am.devvibes.buyandsell.dto.autoMark.AutoMarkDto;
import am.devvibes.buyandsell.dto.autoModel.AutoModelDto;
import am.devvibes.buyandsell.dto.generation.GenerationDto;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.auto.GenerationEntity;

import java.util.List;

public interface ConstCategoryService {

	List<AutoMarkDto> findMarksByCategory(Long categoryId);

	List<AutoModelDto> findModelsByMark(Long markId);

	List<GenerationDto> findGenerationsByModel(Long modelId);

	List<String> findByFieldNameId(Long id);

}
