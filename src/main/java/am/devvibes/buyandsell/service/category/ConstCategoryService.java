package am.devvibes.buyandsell.service.category;

import am.devvibes.buyandsell.dto.electronic.electronicMark.ElectronicMarkDto;
import am.devvibes.buyandsell.dto.electronic.electronicModel.ElectronicModelDto;
import am.devvibes.buyandsell.dto.vehicle.vehicleMark.VehicleMarkDto;
import am.devvibes.buyandsell.dto.vehicle.vehicleModel.VehicleModelDto;
import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.bus.BusMarkEntity;
import am.devvibes.buyandsell.entity.bus.BusModelEntity;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneMarkEntity;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneModelEntity;
import am.devvibes.buyandsell.entity.notebook.NotebookMarkEntity;
import am.devvibes.buyandsell.entity.truck.TruckMarkEntity;
import am.devvibes.buyandsell.entity.truck.TruckModelEntity;

import java.util.List;

public interface ConstCategoryService {

	List<AutoMarkEntity> findAutoMarks();

	List<TruckMarkEntity> findTruckMarks();

	List<BusMarkEntity> findBusMarks();

	List<MobilePhoneMarkEntity> findMobileMarks();

	List<NotebookMarkEntity> findNotebookMarks();

	List<AutoModelEntity> findAutoModelsByMark(Long markId);

	List<TruckModelEntity> findTruckModelsByMark(Long markId);

	List<BusModelEntity> findBusModelsByMark(Long markId);

	List<MobilePhoneModelEntity> findMobileModelsByMark(Long markId);

	List<String> findByFieldNameId(Long id);

}
