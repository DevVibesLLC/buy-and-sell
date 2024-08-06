package am.devvibes.buyandsell.controller;

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
import am.devvibes.buyandsell.entity.motorcycle.MotorcycleMarkEntity;
import am.devvibes.buyandsell.entity.notebook.NotebookMarkEntity;
import am.devvibes.buyandsell.entity.truck.TruckMarkEntity;
import am.devvibes.buyandsell.entity.truck.TruckModelEntity;
import am.devvibes.buyandsell.mapper.auto.autoMark.AutoMarkMapper;
import am.devvibes.buyandsell.mapper.auto.autoModel.AutoModelMapper;
import am.devvibes.buyandsell.mapper.bus.busMark.BusMarkMapper;
import am.devvibes.buyandsell.mapper.bus.busModel.BusModelMapper;
import am.devvibes.buyandsell.mapper.mobile.mobileMark.MobileMarkMapper;
import am.devvibes.buyandsell.mapper.mobile.mobileModel.MobileModelMapper;
import am.devvibes.buyandsell.mapper.motorcycle.motorcycleMark.MotorcycleMarkMapper;
import am.devvibes.buyandsell.mapper.notebook.notebookMark.NotebookMarkMapper;
import am.devvibes.buyandsell.mapper.truck.truckMark.TruckMarkMapper;
import am.devvibes.buyandsell.mapper.truck.truckModel.TruckModelMapper;
import am.devvibes.buyandsell.service.category.ConstCategoryService;
import am.devvibes.buyandsell.util.LocationEnum;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/const")
public class ConstController {

	private final ConstCategoryService constCategoryService;
	private final AutoMarkMapper autoMarkMapper;
	private final AutoModelMapper autoModelMapper;
	private final TruckMarkMapper truckMarkMapper;
	private final TruckModelMapper truckModelMapper;
	private final MotorcycleMarkMapper motorcycleMarkMapper;
	private final BusMarkMapper busMarkMapper;
	private final BusModelMapper busModelMapper;
	private final MobileMarkMapper mobileMarkMapper;
	private final MobileModelMapper mobileModelMapper;
	private final NotebookMarkMapper notebookMarkMapper;

	@GetMapping("/category/auto/marks")
	@Operation(summary = "Get all auto marks by category id")
	public ResponseEntity<List<VehicleMarkDto>> getAllAutoMarks() {
		List<AutoMarkEntity> autoMarkEntities = constCategoryService.findAutoMarks();
		return ResponseEntity.ok(autoMarkMapper.mapEntityListToDtoList(autoMarkEntities));
	}

	@GetMapping("/category/truck/marks")
	@Operation(summary = "Get all truck marks by category id")
	public ResponseEntity<List<VehicleMarkDto>> getAllTruckMarks() {
		List<TruckMarkEntity> truckMarkEntities = constCategoryService.findTruckMarks();
		return ResponseEntity.ok(truckMarkMapper.mapEntityListToDtoList(truckMarkEntities));
	}

	@GetMapping("/category/bus/marks")
	@Operation(summary = "Get all bus marks by category id")
	public ResponseEntity<List<VehicleMarkDto>> getAllBusMarks() {
		List<BusMarkEntity> busMarkEntities = constCategoryService.findBusMarks();
		return ResponseEntity.ok(busMarkMapper.mapEntityListToDtoList(busMarkEntities));
	}

	@GetMapping("/category/mobile/marks")
	@Operation(summary = "Get all mobile phone marks by category id")
	public ResponseEntity<List<ElectronicMarkDto>> getAllMobileMarks() {
		List<MobilePhoneMarkEntity> mobilePhoneMarkEntities = constCategoryService.findMobileMarks();
		return ResponseEntity.ok(mobileMarkMapper.mapEntityListToDtoList(mobilePhoneMarkEntities));
	}

	@GetMapping("/category/notebook/marks")
	@Operation(summary = "Get all notebook marks by category id")
	public ResponseEntity<List<ElectronicMarkDto>> getAllNotebookMarks() {
		List<NotebookMarkEntity> notebookMarkEntities = constCategoryService.findNotebookMarks();
		return ResponseEntity.ok(notebookMarkMapper.mapEntityListToDtoList(notebookMarkEntities));
	}

	@GetMapping("/autoMark/{markId}/models")
	@Operation(summary = "Get all auto models by mark id")
	public ResponseEntity<List<VehicleModelDto>> getAllAutoModels(@PathVariable Long markId) {
		List<AutoModelEntity> autoModelsByMark = constCategoryService.findAutoModelsByMark(markId);
		return ResponseEntity.ok(autoModelMapper.mapEntityListToDtoList(autoModelsByMark));
	}

	@GetMapping("/truckMark/{markId}/models")
	@Operation(summary = "Get all truck models by mark id")
	public ResponseEntity<List<VehicleModelDto>> getAllTruckModels(@PathVariable Long markId) {
		List<TruckModelEntity> truckModelsByMark = constCategoryService.findTruckModelsByMark(markId);
		return ResponseEntity.ok(truckModelMapper.mapEntityListToDtoList(truckModelsByMark));
	}

	@GetMapping("/busMark/{markId}/models")
	@Operation(summary = "Get all bus models by mark id")
	public ResponseEntity<List<VehicleModelDto>> getAllBusModels(@PathVariable Long markId) {
		List<BusModelEntity> busModelsByMark = constCategoryService.findBusModelsByMark(markId);
		return ResponseEntity.ok(busModelMapper.mapEntityListToDtoList(busModelsByMark));
	}

	@GetMapping("/mobileMark/{markId}/models")
	@Operation(summary = "Get all mobile phone models by mark id")
	public ResponseEntity<List<ElectronicModelDto>> getAllMobileModels(@PathVariable Long markId) {
		List<MobilePhoneModelEntity> mobileModelsByMark = constCategoryService.findMobileModelsByMark(markId);
		return ResponseEntity.ok(mobileModelMapper.mapEntityListToDtoList(mobileModelsByMark));
	}

	@GetMapping("/category/motorcycle/marks")
	@Operation(summary = "Get all motorcycle marks by category id")
	public ResponseEntity<List<VehicleMarkDto>> getAllMotorcycleMarks() {
		List<MotorcycleMarkEntity> motorcycleMarkEntities = constCategoryService.findMotorcycleMarks();
		return ResponseEntity.ok(motorcycleMarkMapper.mapEntityListToDtoList(motorcycleMarkEntities));
	}

	@GetMapping("/locations")
	@Operation(summary = "Get all locations")
	public ResponseEntity<List<LocationEnum>> getAllLocations() {
		return ResponseEntity.ok(Arrays.stream(LocationEnum.values()).toList());
	}

	/*@GetMapping("field/{fieldId}")
	@Operation(summary = "Get fields by field name id")
	public ResponseEntity<List<String>> getFieldValues(@PathVariable Long fieldId) {
		return ResponseEntity.ok(constCategoryService.findByFieldNameId(fieldId));
	}*/

}
