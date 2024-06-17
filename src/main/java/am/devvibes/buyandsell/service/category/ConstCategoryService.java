package am.devvibes.buyandsell.service.category;

import am.devvibes.buyandsell.dto.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.dto.vehicleModel.VehicleModelDto;

import java.util.List;

public interface ConstCategoryService {

	List<VehicleMarkDto> findAutoMarks();

	List<VehicleMarkDto> findTruckMarks();

	List<VehicleMarkDto> findBusMarks();

	List<VehicleModelDto> findAutoModelsByMark(Long markId);

	List<VehicleModelDto> findTruckModelsByMark(Long markId);

	List<VehicleModelDto> findBusModelsByMark(Long markId);

	List<String> findByFieldNameId(Long id);

}
