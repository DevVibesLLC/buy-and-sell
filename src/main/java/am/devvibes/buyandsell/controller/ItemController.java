package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.filter.*;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.search.SearchDto;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.util.page.CustomPageRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/items")
public class ItemController {

	private final ItemService itemService;

	@PostMapping(value = "/{categoryId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Create Item")
	public ResponseEntity<ItemResponseDto> createItem(@PathVariable Long categoryId,
			@RequestPart(value = "dto") ItemRequestDto itemRequestDto,
			@RequestParam(value = "images") List<MultipartFile> images) {
		return ResponseEntity.ok(itemService.save(itemRequestDto, images, categoryId));
	}

	@GetMapping("/category/{categoryId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Get Item by Category Id")
	public ResponseEntity<List<ItemResponseDto>> getItemsByCategoryId(@PathVariable Long categoryId) {
		return ResponseEntity.ok(itemService.findItemsByCategory(categoryId));
	}

	@PutMapping("/{categoryId}/item/{itemId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Update Item")
	public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long categoryId,
			@PathVariable Long itemId,
			@RequestBody ItemRequestDto itemRequestDto) {
		return ResponseEntity.ok(itemService.update(itemRequestDto, categoryId, itemId));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get Item by Id")
	public ResponseEntity<ItemResponseDto> getItemById(@PathVariable Long id) {
		return ResponseEntity.ok(itemService.findById(id));
	}

	@GetMapping
	@Operation(summary = "Get All Items")
	public ResponseEntity<Page<ItemResponseDto>> getAllItem(@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		PageRequest pageRequest = CustomPageRequest.from(page, size, Sort.unsorted());
		return ResponseEntity.ok(itemService.findAllItems(pageRequest));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Delete Item by Id")
	public ResponseEntity<List<ItemResponseDto>> deleteItemById(@PathVariable Long id) {
		itemService.deleteById(id);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/search")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Search Items")
	public ResponseEntity<List<ItemResponseDto>> searchItem(@RequestBody SearchDto searchDto) {
		return ResponseEntity.ok(itemService.searchItems(searchDto));
	}

	@PostMapping("/auto/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Auto Items")
	public ResponseEntity<List<ItemResponseDto>> filterAutoItems(@RequestBody AutoFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/truck/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Truck Items")
	public ResponseEntity<List<ItemResponseDto>> filterTruckItems(@RequestBody TruckFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/bus/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Bus Items")
	public ResponseEntity<List<ItemResponseDto>> filterBusItems(@RequestBody BusFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/apartmentBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Apartment Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterApartmentBuyItems(@RequestBody ApartmentBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/apartmentRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Apartment Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterApartmentRentalItems(
			@RequestBody ApartmentRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/houseBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter House Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterHouseBuyItems(@RequestBody HouseBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/houseRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter House Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterHouseRentalItems(@RequestBody HouseRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/commercialBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Commercial Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterCommercialBuyItems(
			@RequestBody CommercialBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/commercialRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Commercial Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterCommercialRentalItems(
			@RequestBody CommercialRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/garageAndParkingBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garage and Parking Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterGarageAndParkingBuyItems(
			@RequestBody GarageAndParkingBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/garageAndParkingRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garage and Parking Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterGarageAndParkingRentalItems(
			@RequestBody GarageAndParkingRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/landBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Land Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterLandBuyItems(@RequestBody LandBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/landRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Land Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterLandRentalItems(@RequestBody LandRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/newConstructionApartment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter New Construction Apartment Items")
	public ResponseEntity<List<ItemResponseDto>> filterNewConstructionApartmentItems(
			@RequestBody NewConstructionApartmentFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/newConstructionHouse/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter New Construction House Items")
	public ResponseEntity<List<ItemResponseDto>> filterNewConstructionHouseItems(
			@RequestBody NewConstructionHouseFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/apartmentDailyRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Apartment Daily Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterApartmentDailyRentalItems(
			@RequestBody ApartmentDailyRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/houseDailyRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter House Daily Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterHouseDailyRentalItems(
			@RequestBody HouseDailyRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/mobile/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Mobile Phone Items")
	public ResponseEntity<List<ItemResponseDto>> filterMobilePhoneItems(@RequestBody MobilePhoneFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/notebook/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Notebook Items")
	public ResponseEntity<List<ItemResponseDto>> filterNotebookItems(@RequestBody NotebookFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/computer/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Computer Items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerItems(@RequestBody ComputerFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/smartWatch/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Smart Watch Items")
	public ResponseEntity<List<ItemResponseDto>> filterSmartWatchItems(@RequestBody SmartWatchFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/tablet/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter tablet items")
	public ResponseEntity<List<ItemResponseDto>> filterTabletItems(@RequestBody TabletFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/tv/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter TV Items")
	public ResponseEntity<List<ItemResponseDto>> filterTVItems(@RequestBody TVFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/gamingConsole/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Gaming Console Items")
	public ResponseEntity<List<ItemResponseDto>> filterGamingConsoleItems(
			@RequestBody GamingConsoleFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/headphone/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Headphone Items")
	public ResponseEntity<List<ItemResponseDto>> filterHeadphoneItems(@RequestBody HeadphoneFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/computerAndNotebookParts/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Computer and Notebook Parts Items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerAndNotebookPartsItems(
			@RequestBody ComputerAndNotebookPartsFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/photoAndVideoCamera/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Photo and Video Camera Items")
	public ResponseEntity<List<ItemResponseDto>> filterPhotoAndVideoCameraItems(
			@RequestBody PhotoAndVideoCameraFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/computerGames/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Computer Games Items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerGamesItems(
			@RequestBody ComputerGamesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/smartHomeAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Smart Home Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterSmartHomeAccessoriesItems(
			@RequestBody SmartHomeAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/washer/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Washer Items")
	public ResponseEntity<List<ItemResponseDto>> filterWasherItems(@RequestBody WasherFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/clothesDryer/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Clothes Dryer Items")
	public ResponseEntity<List<ItemResponseDto>> filterClothesDryerItems(@RequestBody ClothesDryerFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/ironAndAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Iron and Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterIronAndAccessoriesItems(
			@RequestBody IronAndAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/refrigerator/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Refrigerator Items")
	public ResponseEntity<List<ItemResponseDto>> filterRefrigeratorItems(@RequestBody RefrigeratorFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/freezer/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Freezer Items")
	public ResponseEntity<List<ItemResponseDto>> filterFreezerItems(@RequestBody FreezerFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/dishwasher/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Dishwasher Items")
	public ResponseEntity<List<ItemResponseDto>> filterDishwasherItems(@RequestBody DishwasherFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/microwave/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Microwave Items")
	public ResponseEntity<List<ItemResponseDto>> filterMicrowaveItems(@RequestBody MicrowaveFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/stove/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Stove Items")
	public ResponseEntity<List<ItemResponseDto>> filterStoveItems(@RequestBody StoveFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/coffeeMakerAndAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Coffee Maker and Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterCoffeeMakerAndAccessoriesItems(
			@RequestBody CoffeeMakerAndAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/kettle/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Kettle Items")
	public ResponseEntity<List<ItemResponseDto>> filterKettleItems(@RequestBody KettleFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/rangeHood/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter range hood Items")
	public ResponseEntity<List<ItemResponseDto>> filterRangeHoodItems(@RequestBody RangeHoodFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/vacuumCleaner/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Vacuum Cleaner Items")
	public ResponseEntity<List<ItemResponseDto>> filterVacuumCleanerItems(
			@RequestBody VacuumCleanerFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/roboticVacuum/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Robotic Vacuum Items")
	public ResponseEntity<List<ItemResponseDto>> filterRoboticVacuumItems(
			@RequestBody RoboticVacuumFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/floorWasher/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Floor Washer Items")
	public ResponseEntity<List<ItemResponseDto>> filterFloorWasherItems(@RequestBody FloorWasherFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/airConditioner/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Air Conditioner Items")
	public ResponseEntity<List<ItemResponseDto>> filterAirConditionerItems(
			@RequestBody AirConditionerFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/waterHeater/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Water Heater Items")
	public ResponseEntity<List<ItemResponseDto>> filterWaterHeaterItems(@RequestBody WaterHeatersFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/airPurifiersAndHumidifiers/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Air Purifiers And Humidifiers Items")
	public ResponseEntity<List<ItemResponseDto>> filterAirPurifiersAndHumidifiersItems(
			@RequestBody AirPurifiersAndHumidifiersFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/computerPeripheral/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Computer Peripheral Items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerPeripheralItems(
			@RequestBody ComputerPeripheralFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/audioPlayerAndStereo/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Audio Player and Stereo Items")
	public ResponseEntity<List<ItemResponseDto>> filterAudioPlayerAndStereoItems(
			@RequestBody AudioPlayerAndStereoFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/quadcopterAndDrone/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Quadcopters and Drones Items")
	public ResponseEntity<List<ItemResponseDto>> filterQuadcoptersAndDronesItems(
			@RequestBody QuadcoptersAndDronesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/sofaAndArmchair/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Sofa and Armchair Items")
	public ResponseEntity<List<ItemResponseDto>> filterSofaAndArmchairItems(
			@RequestBody SofaAndArmchairFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/storage/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Storage Items")
	public ResponseEntity<List<ItemResponseDto>> filterStorageItems(@RequestBody StorageFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/tableAndChair/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Table and Chair Items")
	public ResponseEntity<List<ItemResponseDto>> filterTableAndChairItems(
			@RequestBody TableAndChairFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/bedroomFurniture/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Bedroom Furniture Items")
	public ResponseEntity<List<ItemResponseDto>> filterBedroomFurnitureItems(
			@RequestBody BedroomFurnitureFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/kitchenFurniture/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Kitchen Furniture Items")
	public ResponseEntity<List<ItemResponseDto>> filterKitchenFurnitureItems(
			@RequestBody KitchenFurnitureFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/gardenFurniture/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garden Furniture Items")
	public ResponseEntity<List<ItemResponseDto>> filterGardenFurnitureItems(
			@RequestBody GardenFurnitureFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/barbecueAndAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Barbecue And Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterBarbecueAndAccessoriesItems(
			@RequestBody BarbecueAndAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/gardenDecor/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garden Decor Items")
	public ResponseEntity<List<ItemResponseDto>> filterGardenDecorItems(@RequestBody GardenDecorFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/gardenAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garden Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterGardenAccessoriesItems(
			@RequestBody GardenAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/lighting/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Lighting Items")
	public ResponseEntity<List<ItemResponseDto>> filterLightingItems(@RequestBody LightingFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/textile/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Textile Items")
	public ResponseEntity<List<ItemResponseDto>> filterTextileItems(@RequestBody TextileFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/rug/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Rug Items")
	public ResponseEntity<List<ItemResponseDto>> filterRugItems(@RequestBody RugFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/interiorDecoration/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Interior Decoration Items")
	public ResponseEntity<List<ItemResponseDto>> filterInteriorDecorationItems(
			@RequestBody InteriorDecorationFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/tableware/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Tableware Items")
	public ResponseEntity<List<ItemResponseDto>> filterTablewareItems(@RequestBody TablewareFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/cookingAndBaking/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Cooking and Baking Items")
	public ResponseEntity<List<ItemResponseDto>> filterCookingAndBakingItems(
			@RequestBody CookingAndBakingFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/kitchenAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Kitchen Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterKitchenAccessoriesItems(
			@RequestBody KitchenAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/bathroomAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Bathroom Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterBathroomAccessoriesItems(
			@RequestBody BathroomAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/videoSurveillance/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Video Surveillance Items")
	public ResponseEntity<List<ItemResponseDto>> filterVideoSurveillanceItems(
			@RequestBody VideoSurveillanceFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/carPart/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Part Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarPartItems(@RequestBody CarPartFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/wheelAndTire/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Wheel and Tire Items")
	public ResponseEntity<List<ItemResponseDto>> filterWheelAndTireItems(@RequestBody WheelAndTireFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/rimAndHubCap/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Rim and Hub Cap Items")
	public ResponseEntity<List<ItemResponseDto>> filterRimAndHubCapItems(@RequestBody RimAndHubCapFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/carBattery/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Battery Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarBatteryItems(@RequestBody CarBatteryFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/gasEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Gas Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterGasEquipmentItems(@RequestBody GasEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/oilAndChemical/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Oil and Chemical Items")
	public ResponseEntity<List<ItemResponseDto>> filterOilAndChemicalItems(
			@RequestBody OilAndChemicalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/carAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarAccessoriesItems(
			@RequestBody CarAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/carElectronic/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Electronic Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarElectronicItems(
			@RequestBody CarElectronicFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/carAudioAndVideo/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Audio and Video Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarAudioAndVideoItems(
			@RequestBody CarAudioAndVideoFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/personalTransportation/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Personal Transportation Items")
	public ResponseEntity<List<ItemResponseDto>> filterPersonalTransportationItems(
			@RequestBody PersonalTransportationFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/atvAndSnowmobile/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter ATV and Snowmobile Items")
	public ResponseEntity<List<ItemResponseDto>> filterAtvAndSnowmobileItems(
			@RequestBody AtvAndSnowmobileFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/boatAndWaterTransport/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Boats and Water Transport Items")
	public ResponseEntity<List<ItemResponseDto>> filterBoatAndWaterTransportItems(
			@RequestBody BoatAndWaterTransportFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/trailerAndBooth/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Trailer and Booth Items")
	public ResponseEntity<List<ItemResponseDto>> filterTrailerAndBoothItems(
			@RequestBody TrailerAndBoothFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/eventVenueRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter EventVenue Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterEventVenueRentalItems(
			@RequestBody EventVenueRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

}