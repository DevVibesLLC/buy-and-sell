package am.devvibes.buyandsell.service.category;

import am.devvibes.buyandsell.dto.electronic.electronicMark.ElectronicMarkDto;
import am.devvibes.buyandsell.dto.electronic.electronicModel.ElectronicModelDto;
import am.devvibes.buyandsell.dto.vehicle.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.dto.vehicle.vehicleModel.VehicleModelDto;

import java.util.List;

public interface ConstCategoryService {

	List<VehicleMarkDto> findAutoMarks();

	List<VehicleMarkDto> findTruckMarks();

	List<VehicleMarkDto> findBusMarks();

	List<ElectronicMarkDto> findMobileMarks();

	List<VehicleModelDto> findAutoModelsByMark(Long markId);

	List<VehicleModelDto> findTruckModelsByMark(Long markId);

	List<VehicleModelDto> findBusModelsByMark(Long markId);

	List<ElectronicModelDto> findMobileModelsByMark(Long markId);

	List<String> findByFieldNameId(Long id);

}
