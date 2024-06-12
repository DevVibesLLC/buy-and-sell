package am.devvibes.buyandsell.service.category;

import am.devvibes.buyandsell.dto.autoMark.VehicleMarkDto;
import am.devvibes.buyandsell.dto.autoModel.VehicleModelDto;

import java.util.List;

public interface ConstCategoryService {

	List<VehicleMarkDto> findMarksByCategory(Long categoryId);

	List<VehicleModelDto> findAutoModelsByMark(Long markId);

	List<VehicleMarkDto> findTruckMarksByCategory(Long categoryId);

	List<VehicleModelDto> findTruckModelsByMark(Long markId);

	List<String> findByFieldNameId(Long id);

}
