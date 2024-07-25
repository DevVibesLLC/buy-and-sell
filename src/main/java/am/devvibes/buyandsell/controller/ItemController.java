package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.filter.*;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.item.ItemUpdateDto;
import am.devvibes.buyandsell.dto.search.SearchDto;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/items")
public class ItemController {

	private final ItemService itemService;
	private final ItemMapper itemMapper;

	@PostMapping(value = "/{categoryId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Create Item")
	public ResponseEntity<ItemResponseDto> createItem(@PathVariable Long categoryId,
			@RequestBody ItemRequestDto itemRequestDto) {
		return ResponseEntity.ok(itemMapper.mapEntityToDto(itemService.save(itemRequestDto, categoryId)));
	}

	@GetMapping("/category/{categoryId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Get Item by Category Id")
	public ResponseEntity<List<ItemResponseDto>> getItemsByCategoryId(@PathVariable Long categoryId) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.findItemsByCategory(categoryId)));
	}

	@PutMapping("/{categoryId}/item/{itemId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Update Item")
	public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long categoryId,
			@PathVariable Long itemId,
			@RequestBody ItemUpdateDto itemUpdateDto) {
		return ResponseEntity.ok(itemMapper.mapEntityToDto(itemService.update(itemUpdateDto, categoryId, itemId)));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get Item by Id")
	public ResponseEntity<ItemResponseDto> getItemById(@PathVariable Long id) {
		return ResponseEntity.ok(itemMapper.mapEntityToDto(itemService.findById(id)));
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
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.searchItems(searchDto)));
	}

	@PostMapping("/auto/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Auto Items")
	public ResponseEntity<List<ItemResponseDto>> filterAutoItems(@RequestBody AutoFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/truck/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Truck Items")
	public ResponseEntity<List<ItemResponseDto>> filterTruckItems(@RequestBody TruckFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/bus/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Bus Items")
	public ResponseEntity<List<ItemResponseDto>> filterBusItems(@RequestBody BusFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/apartmentBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Apartment Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterApartmentBuyItems(@RequestBody ApartmentBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/apartmentRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Apartment Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterApartmentRentalItems(
			@RequestBody ApartmentRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/houseBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter House Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterHouseBuyItems(@RequestBody HouseBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/houseRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter House Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterHouseRentalItems(@RequestBody HouseRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/commercialBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Commercial Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterCommercialBuyItems(
			@RequestBody CommercialBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/commercialRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Commercial Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterCommercialRentalItems(
			@RequestBody CommercialRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/garageAndParkingBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garage and Parking Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterGarageAndParkingBuyItems(
			@RequestBody GarageAndParkingBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/garageAndParkingRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garage and Parking Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterGarageAndParkingRentalItems(
			@RequestBody GarageAndParkingRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/landBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Land Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterLandBuyItems(@RequestBody LandBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/landRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Land Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterLandRentalItems(@RequestBody LandRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/newConstructionApartment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter New Construction Apartment Items")
	public ResponseEntity<List<ItemResponseDto>> filterNewConstructionApartmentItems(
			@RequestBody NewConstructionApartmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/newConstructionHouse/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter New Construction House Items")
	public ResponseEntity<List<ItemResponseDto>> filterNewConstructionHouseItems(
			@RequestBody NewConstructionHouseFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/apartmentDailyRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Apartment Daily Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterApartmentDailyRentalItems(
			@RequestBody ApartmentDailyRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/houseDailyRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter House Daily Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterHouseDailyRentalItems(
			@RequestBody HouseDailyRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/mobile/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Mobile Phone Items")
	public ResponseEntity<List<ItemResponseDto>> filterMobilePhoneItems(@RequestBody MobilePhoneFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/notebook/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Notebook Items")
	public ResponseEntity<List<ItemResponseDto>> filterNotebookItems(@RequestBody NotebookFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/computer/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Computer Items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerItems(@RequestBody ComputerFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/smartWatch/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Smart Watch Items")
	public ResponseEntity<List<ItemResponseDto>> filterSmartWatchItems(@RequestBody SmartWatchFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/tablet/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter tablet items")
	public ResponseEntity<List<ItemResponseDto>> filterTabletItems(@RequestBody TabletFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/tv/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter TV Items")
	public ResponseEntity<List<ItemResponseDto>> filterTVItems(@RequestBody TVFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/gamingConsole/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Gaming Console Items")
	public ResponseEntity<List<ItemResponseDto>> filterGamingConsoleItems(
			@RequestBody GamingConsoleFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/headphone/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Headphone Items")
	public ResponseEntity<List<ItemResponseDto>> filterHeadphoneItems(@RequestBody HeadphoneFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/computerAndNotebookParts/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Computer and Notebook Parts Items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerAndNotebookPartsItems(
			@RequestBody ComputerAndNotebookPartsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/photoAndVideoCamera/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Photo and Video Camera Items")
	public ResponseEntity<List<ItemResponseDto>> filterPhotoAndVideoCameraItems(
			@RequestBody PhotoAndVideoCameraFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/computerGames/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Computer Games Items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerGamesItems(
			@RequestBody ComputerGamesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/smartHomeAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Smart Home Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterSmartHomeAccessoriesItems(
			@RequestBody SmartHomeAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/washer/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Washer Items")
	public ResponseEntity<List<ItemResponseDto>> filterWasherItems(@RequestBody WasherFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/clothesDryer/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Clothes Dryer Items")
	public ResponseEntity<List<ItemResponseDto>> filterClothesDryerItems(@RequestBody ClothesDryerFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/ironAndAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Iron and Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterIronAndAccessoriesItems(
			@RequestBody IronAndAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/refrigerator/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Refrigerator Items")
	public ResponseEntity<List<ItemResponseDto>> filterRefrigeratorItems(@RequestBody RefrigeratorFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/freezer/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Freezer Items")
	public ResponseEntity<List<ItemResponseDto>> filterFreezerItems(@RequestBody FreezerFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/dishwasher/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Dishwasher Items")
	public ResponseEntity<List<ItemResponseDto>> filterDishwasherItems(@RequestBody DishwasherFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/microwave/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Microwave Items")
	public ResponseEntity<List<ItemResponseDto>> filterMicrowaveItems(@RequestBody MicrowaveFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/stove/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Stove Items")
	public ResponseEntity<List<ItemResponseDto>> filterStoveItems(@RequestBody StoveFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/coffeeMakerAndAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Coffee Maker and Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterCoffeeMakerAndAccessoriesItems(
			@RequestBody CoffeeMakerAndAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/kettle/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Kettle Items")
	public ResponseEntity<List<ItemResponseDto>> filterKettleItems(@RequestBody KettleFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/rangeHood/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter range hood Items")
	public ResponseEntity<List<ItemResponseDto>> filterRangeHoodItems(@RequestBody RangeHoodFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/vacuumCleaner/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Vacuum Cleaner Items")
	public ResponseEntity<List<ItemResponseDto>> filterVacuumCleanerItems(
			@RequestBody VacuumCleanerFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/roboticVacuum/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Robotic Vacuum Items")
	public ResponseEntity<List<ItemResponseDto>> filterRoboticVacuumItems(
			@RequestBody RoboticVacuumFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/floorWasher/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Floor Washer Items")
	public ResponseEntity<List<ItemResponseDto>> filterFloorWasherItems(@RequestBody FloorWasherFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/airConditioner/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Air Conditioner Items")
	public ResponseEntity<List<ItemResponseDto>> filterAirConditionerItems(
			@RequestBody AirConditionerFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/waterHeater/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Water Heater Items")
	public ResponseEntity<List<ItemResponseDto>> filterWaterHeaterItems(@RequestBody WaterHeatersFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/airPurifiersAndHumidifiers/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Air Purifiers And Humidifiers Items")
	public ResponseEntity<List<ItemResponseDto>> filterAirPurifiersAndHumidifiersItems(
			@RequestBody AirPurifiersAndHumidifiersFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/computerPeripheral/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Computer Peripheral Items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerPeripheralItems(
			@RequestBody ComputerPeripheralFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/audioPlayerAndStereo/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Audio Player and Stereo Items")
	public ResponseEntity<List<ItemResponseDto>> filterAudioPlayerAndStereoItems(
			@RequestBody AudioPlayerAndStereoFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/quadcopterAndDrone/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Quadcopters and Drones Items")
	public ResponseEntity<List<ItemResponseDto>> filterQuadcoptersAndDronesItems(
			@RequestBody QuadcoptersAndDronesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/sofaAndArmchair/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Sofa and Armchair Items")
	public ResponseEntity<List<ItemResponseDto>> filterSofaAndArmchairItems(
			@RequestBody SofaAndArmchairFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/storage/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Storage Items")
	public ResponseEntity<List<ItemResponseDto>> filterStorageItems(@RequestBody StorageFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/tableAndChair/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Table and Chair Items")
	public ResponseEntity<List<ItemResponseDto>> filterTableAndChairItems(
			@RequestBody TableAndChairFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/bedroomFurniture/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Bedroom Furniture Items")
	public ResponseEntity<List<ItemResponseDto>> filterBedroomFurnitureItems(
			@RequestBody BedroomFurnitureFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/kitchenFurniture/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Kitchen Furniture Items")
	public ResponseEntity<List<ItemResponseDto>> filterKitchenFurnitureItems(
			@RequestBody KitchenFurnitureFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/gardenFurniture/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garden Furniture Items")
	public ResponseEntity<List<ItemResponseDto>> filterGardenFurnitureItems(
			@RequestBody GardenFurnitureFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/barbecueAndAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Barbecue And Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterBarbecueAndAccessoriesItems(
			@RequestBody BarbecueAndAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/gardenDecor/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garden Decor Items")
	public ResponseEntity<List<ItemResponseDto>> filterGardenDecorItems(@RequestBody GardenDecorFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/gardenAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garden Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterGardenAccessoriesItems(
			@RequestBody GardenAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/lighting/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Lighting Items")
	public ResponseEntity<List<ItemResponseDto>> filterLightingItems(@RequestBody LightingFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/textile/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Textile Items")
	public ResponseEntity<List<ItemResponseDto>> filterTextileItems(@RequestBody TextileFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/rug/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Rug Items")
	public ResponseEntity<List<ItemResponseDto>> filterRugItems(@RequestBody RugFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/interiorDecoration/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Interior Decoration Items")
	public ResponseEntity<List<ItemResponseDto>> filterInteriorDecorationItems(
			@RequestBody InteriorDecorationFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/tableware/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Tableware Items")
	public ResponseEntity<List<ItemResponseDto>> filterTablewareItems(@RequestBody TablewareFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/cookingAndBaking/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Cooking and Baking Items")
	public ResponseEntity<List<ItemResponseDto>> filterCookingAndBakingItems(
			@RequestBody CookingAndBakingFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/kitchenAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Kitchen Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterKitchenAccessoriesItems(
			@RequestBody KitchenAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/bathroomAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Bathroom Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterBathroomAccessoriesItems(
			@RequestBody BathroomAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/videoSurveillance/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Video Surveillance Items")
	public ResponseEntity<List<ItemResponseDto>> filterVideoSurveillanceItems(
			@RequestBody VideoSurveillanceFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/carPart/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Part Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarPartItems(@RequestBody CarPartFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/wheelAndTire/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Wheel and Tire Items")
	public ResponseEntity<List<ItemResponseDto>> filterWheelAndTireItems(@RequestBody WheelAndTireFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/rimAndHubCap/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Rim and Hub Cap Items")
	public ResponseEntity<List<ItemResponseDto>> filterRimAndHubCapItems(@RequestBody RimAndHubCapFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/carBattery/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Battery Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarBatteryItems(@RequestBody CarBatteryFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/gasEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Gas Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterGasEquipmentItems(@RequestBody GasEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/oilAndChemical/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Oil and Chemical Items")
	public ResponseEntity<List<ItemResponseDto>> filterOilAndChemicalItems(
			@RequestBody OilAndChemicalFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/carAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarAccessoriesItems(
			@RequestBody CarAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/carElectronic/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Electronic Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarElectronicItems(
			@RequestBody CarElectronicFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/carAudioAndVideo/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Audio and Video Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarAudioAndVideoItems(
			@RequestBody CarAudioAndVideoFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/personalTransportation/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Personal Transportation Items")
	public ResponseEntity<List<ItemResponseDto>> filterPersonalTransportationItems(
			@RequestBody PersonalTransportationFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/atvAndSnowmobile/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter ATV and Snowmobile Items")
	public ResponseEntity<List<ItemResponseDto>> filterAtvAndSnowmobileItems(
			@RequestBody AtvAndSnowmobileFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/boatAndWaterTransport/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Boats and Water Transport Items")
	public ResponseEntity<List<ItemResponseDto>> filterBoatAndWaterTransportItems(
			@RequestBody BoatAndWaterTransportFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/trailerAndBooth/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Trailer and Booth Items")
	public ResponseEntity<List<ItemResponseDto>> filterTrailerAndBoothItems(
			@RequestBody TrailerAndBoothFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/eventVenueRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Event Venue Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterEventVenueRentalItems(
			@RequestBody EventVenueRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/womenClothingBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Women Clothing Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterWomenClothingBuyItems(
			@RequestBody WomenClothingBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/womenClothingRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Women Clothing Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterWomenClothingRentalItems(
			@RequestBody WomenClothingRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/womenShoes/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Women Shoes Items")
	public ResponseEntity<List<ItemResponseDto>> filterWomenShoesItems(
			@RequestBody WomenShoesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/womenAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Women Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterWomenAccessoriesItems(
			@RequestBody WomenAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/menClothingBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Men Clothing Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterMenClothingBuyItems(
			@RequestBody MenClothingBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/menShoes/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Men Shoes Items")
	public ResponseEntity<List<ItemResponseDto>> filterMenShoesItems(
			@RequestBody MenShoesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/menAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Men Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterMenAccessoriesItems(
			@RequestBody MenAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/jewellery/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Jewellery Items")
	public ResponseEntity<List<ItemResponseDto>> filterJewelleryItems(
			@RequestBody JewelleryFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/glassesAndFrames/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Glasses and Frames Items")
	public ResponseEntity<List<ItemResponseDto>> filterGlassesAndFramesItems(
			@RequestBody GlassesAndFramesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/watches/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Watches Items")
	public ResponseEntity<List<ItemResponseDto>> filterWatchesItems(
			@RequestBody WatchesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/handbagsAndWallets/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Handbags and Wallets Items")
	public ResponseEntity<List<ItemResponseDto>> filterHandbagsAndWalletsItems(
			@RequestBody HandbagsAndWalletsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/workwearAndAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Workwear and Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterWorkwearAndAccessoriesItems(
			@RequestBody WorkwearAndAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/carnivalCostumes/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Carnival Costumes Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarnivalCostumesItems(
			@RequestBody CarnivalCostumesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/weddingDresses/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Wedding Dresses Items")
	public ResponseEntity<List<ItemResponseDto>> filterWeddingDressesItems(
			@RequestBody WeddingDressesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/weddingShoes/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Wedding Shoes Items")
	public ResponseEntity<List<ItemResponseDto>> filterWeddingShoesItems(
			@RequestBody WeddingShoesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/weddingAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Wedding Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterWeddingAccessoriesItems(
			@RequestBody WeddingAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/collectibleItems/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Collectible Items Items")
	public ResponseEntity<List<ItemResponseDto>> filterCollectibleItemsItems(
			@RequestBody CollectibleItemsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/paintingsAndPictures/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Painting and Pictures Items")
	public ResponseEntity<List<ItemResponseDto>> filterPaintingsAndPicturesItems(
			@RequestBody PaintingsAndPicturesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/artsObjects/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Arts Objects Items")
	public ResponseEntity<List<ItemResponseDto>> filterArtsObjectsItems(
			@RequestBody ArtsObjectsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/artsAndCrafts/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Arts and Crafts Items")
	public ResponseEntity<List<ItemResponseDto>> filterArtsAndCraftsItems(
			@RequestBody ArtsAndCraftsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/motorcycle/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Motorcycle Items")
	public ResponseEntity<List<ItemResponseDto>> filterMotorcycleItems(
			@RequestBody MotorcycleFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/motorcyclePartsAndAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Motorcycle Parts and Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterMotorcyclePartsAndAccessoriesItems(
			@RequestBody MotorcyclePartsAndAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/guitars/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Guitars Items")
	public ResponseEntity<List<ItemResponseDto>> filterGuitarsItems(
			@RequestBody GuitarsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/pianosAndKeyboardInstruments/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Pianos and Keyboard Instruments Items")
	public ResponseEntity<List<ItemResponseDto>> filterPianosAndKeyboardInstrumentsItems(
			@RequestBody PianosAndKeyboardInstrumentsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/brassAndWoodwindInstruments/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Brass and Woodwind Instruments Items")
	public ResponseEntity<List<ItemResponseDto>> filterBrassAndWoodwindInstrumentsItems(
			@RequestBody BrassAndWoodwindInstrumentsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/stringInstruments/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter String Instruments Items")
	public ResponseEntity<List<ItemResponseDto>> filterStringInstrumentsItems(
			@RequestBody StringInstrumentsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/accordions/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Accordions Items")
	public ResponseEntity<List<ItemResponseDto>> filterAccordionsItems(
			@RequestBody AccordionsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/drumsAndPercussionInstruments/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Drums and Percussion Instruments Items")
	public ResponseEntity<List<ItemResponseDto>> filterDrumsAndPercussionInstrumentsItems(
			@RequestBody DrumsAndPercussionInstrumentsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/studioAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Studio Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterStudioAccessoriesItems(
			@RequestBody StudioAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/huntingAndFishing/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Hunting and Fishing Items")
	public ResponseEntity<List<ItemResponseDto>> filterHuntingAndFishingItems(
			@RequestBody HuntingAndFishingFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/campingEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Camping Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterCampingEquipmentItems(
			@RequestBody CampingEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/fitnessAndExerciseEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Fitness and Exercise Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterFitnessAndExerciseEquipmentItems(
			@RequestBody FitnessAndExerciseEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/billiardAndBowling/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Billiard and Bowling Items")
	public ResponseEntity<List<ItemResponseDto>> filterBilliardAndBowlingItems(
			@RequestBody BilliardAndBowlingFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/footballAndBallGames/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Football and Ball Games Items")
	public ResponseEntity<List<ItemResponseDto>> filterFootballAndBallGamesItems(
			@RequestBody FootballAndBallGamesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/waterSports/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Water Sports Items")
	public ResponseEntity<List<ItemResponseDto>> filterWaterSportsItems(
			@RequestBody WaterSportsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/winterSportsEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Winter Sports Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterWinterSportsEquipmentItems(
			@RequestBody WinterSportsEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/boxingAndMartialArts/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Boxing and Martial arts Items")
	public ResponseEntity<List<ItemResponseDto>> filterBoxingAndMartialArtsItems(
			@RequestBody BoxingAndMartialArtsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/tennisAndBadminton/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Tennis and Badminton Items")
	public ResponseEntity<List<ItemResponseDto>> filterTennisAndBadmintonItems(
			@RequestBody TennisAndBadmintonFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/mountaineering/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter mountaineering Items")
	public ResponseEntity<List<ItemResponseDto>> filterMountaineeringItems(
			@RequestBody MountaineeringFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/sportsNutrition/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter sports nutrition Items")
	public ResponseEntity<List<ItemResponseDto>> filterSportsNutritionItems(
			@RequestBody SportsNutritionFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/booksAndMagazines/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Books and Magazines Items")
	public ResponseEntity<List<ItemResponseDto>> filterBooksAndMagazinesItems(
			@RequestBody BooksAndMagazinesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/filmsAndMusic/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Films and Music Items")
	public ResponseEntity<List<ItemResponseDto>> filterFilmsAndMusicItems(
			@RequestBody FilmsAndMusicFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/dogs/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Dogs Items")
	public ResponseEntity<List<ItemResponseDto>> filterDogsItems(
			@RequestBody DogsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/cats/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Cats Items")
	public ResponseEntity<List<ItemResponseDto>> filterCatsItems(
			@RequestBody CatsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/fish/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Fish Items")
	public ResponseEntity<List<ItemResponseDto>> filterFishItems(
			@RequestBody FishFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/birds/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Birds Items")
	public ResponseEntity<List<ItemResponseDto>> filterBirdsItems(
			@RequestBody BirdsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/rodents/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Rodents Items")
	public ResponseEntity<List<ItemResponseDto>> filterRodentsItems(
			@RequestBody RodentsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/reptiles/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Reptiles Items")
	public ResponseEntity<List<ItemResponseDto>> filterReptilesItems(
			@RequestBody ReptilesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/cattle/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Cattle Items")
	public ResponseEntity<List<ItemResponseDto>> filterCattleItems(
			@RequestBody CattleFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/horses/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Horses Items")
	public ResponseEntity<List<ItemResponseDto>> filterHorsesItems(
			@RequestBody HorsesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/pigsAndPiglets/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Pigs and Piglets Items")
	public ResponseEntity<List<ItemResponseDto>> filterPigsAndPigletsItems(
			@RequestBody PigsAndPigletsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/sheepAndGoat/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Sheep And Goat Items")
	public ResponseEntity<List<ItemResponseDto>> filterSheepAndGoatItems(
			@RequestBody SheepAndGoatFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/rabbits/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Rabbits Items")
	public ResponseEntity<List<ItemResponseDto>> filterRabbitsItems(
			@RequestBody RabbitsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/girlsClothing/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Girls Clothing Items")
	public ResponseEntity<List<ItemResponseDto>> filterGirlsClothingItems(
			@RequestBody GirlsClothingFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/boysClothing/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Boys Clothing Items")
	public ResponseEntity<List<ItemResponseDto>> filterBoysClothingItems(
			@RequestBody BoysClothingFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/babiesClothing/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Babies Clothing Items")
	public ResponseEntity<List<ItemResponseDto>> filterBabiesClothingItems(
			@RequestBody BabiesClothingFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/shoesForGirls/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Shoe for Girls Items")
	public ResponseEntity<List<ItemResponseDto>> filterShoeForGirlsItems(
			@RequestBody ShoesForGirlsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/shoesForBoys/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Shoe for Boys Items")
	public ResponseEntity<List<ItemResponseDto>> filterShoeForBoysItems(
			@RequestBody ShoesForBoysFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/kidsTransportation/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Kids Transportation Items")
	public ResponseEntity<List<ItemResponseDto>> filterKidsTransportationItems(
			@RequestBody KidsTransportationFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/buildingSets/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Building Sets Items")
	public ResponseEntity<List<ItemResponseDto>> filterBuildingSetsItems(
			@RequestBody BuildingSetsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/learningAndEducationalToys/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Learning and Educational Toys Items")
	public ResponseEntity<List<ItemResponseDto>> filterLearningAndEducationalToysItems(
			@RequestBody LearningAndEducationalToysFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/toysForGirls/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Toys for Girls Items")
	public ResponseEntity<List<ItemResponseDto>> filterToysForGirlsItems(
			@RequestBody ToysForGirlsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/toysForBoys/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Toys for Boys Items")
	public ResponseEntity<List<ItemResponseDto>> filterToysForBoysItems(
			@RequestBody ToysForBoysFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/toysForNewborns/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Toys for Newborns Items")
	public ResponseEntity<List<ItemResponseDto>> filterToysForNewbornsItems(
			@RequestBody ToysForNewbornsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/outdoorsAndSeasonalToys/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Outdoors and Seasonal Toys Items")
	public ResponseEntity<List<ItemResponseDto>> filterOutdoorsAndSeasonalToysItems(
			@RequestBody OutdoorsAndSeasonalToysFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/productsForBabies/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Products for Babies Items")
	public ResponseEntity<List<ItemResponseDto>> filterProductsForBabiesItems(
			@RequestBody ProductsForBabiesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/strollers/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Strollers Items")
	public ResponseEntity<List<ItemResponseDto>> filterStrollersItems(
			@RequestBody StrollersFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/carSeats/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Seats Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarSeatsItems(
			@RequestBody CarSeatsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/babyCarriers/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Baby Carriers Items")
	public ResponseEntity<List<ItemResponseDto>> filterBabyCarriersItems(
			@RequestBody BabyCarriersFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/walkers/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Walkers Items")
	public ResponseEntity<List<ItemResponseDto>> filterWalkersItems(
			@RequestBody WalkersFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/swings/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Swings Items")
	public ResponseEntity<List<ItemResponseDto>> filterSwingsItems(
			@RequestBody SwingsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/playpens/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Playpens Items")
	public ResponseEntity<List<ItemResponseDto>> filterPlaypensItems(
			@RequestBody PlaypensFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/bedAccessoriesAndDecor/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Bed Accessories and Decor Items")
	public ResponseEntity<List<ItemResponseDto>> filterBedAccessoriesAndDecorItems(
			@RequestBody BedAccessoriesAndDecorFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/kidsFurniture/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Kids Furniture Items")
	public ResponseEntity<List<ItemResponseDto>> filterKidsFurnitureItems(
			@RequestBody KidsFurnitureFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/highChairs/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter High Chairs Items")
	public ResponseEntity<List<ItemResponseDto>> filterHighChairsItems(
			@RequestBody HighChairsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/feeding/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Feeding Items")
	public ResponseEntity<List<ItemResponseDto>> filterFeedingItems(
			@RequestBody FeedingFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/bathAndHygiene/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Bath and Hygiene Items")
	public ResponseEntity<List<ItemResponseDto>> filterBathAndHygieneItems(
			@RequestBody BathAndHygieneFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/backpacksAndBags/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Backpacks and Bags Items")
	public ResponseEntity<List<ItemResponseDto>> filterBackpacksAndBagsItems(
			@RequestBody BackpacksAndBagsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/schoolSupplies/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter School Supplies Items")
	public ResponseEntity<List<ItemResponseDto>> filterSchoolSuppliesItems(
			@RequestBody SchoolSuppliesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/productsForDogs/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Products for Dogs Items")
	public ResponseEntity<List<ItemResponseDto>> filterProductsForDogsItems(
			@RequestBody ProductsForDogsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/productsForCats/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Products for Cats Items")
	public ResponseEntity<List<ItemResponseDto>> filterProductsForCatsItems(
			@RequestBody ProductsForCatsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/productsForFishAndReptiles/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Products For Fish and Reptiles Items")
	public ResponseEntity<List<ItemResponseDto>> filterProductsForFishAndReptilesItems(
			@RequestBody ProductsForFishAndReptilesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/productsForRodents/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Products For Rodents Items")
	public ResponseEntity<List<ItemResponseDto>> filterProductsForRodentsItems(
			@RequestBody ProductsForRodentsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/productsForFarmAnimals/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Products For Farm Animals Items")
	public ResponseEntity<List<ItemResponseDto>> filterProductsForFarmAnimalsItems(
			@RequestBody ProductsForFarmAnimalsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/productsForBirds/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Products For Birds Items")
	public ResponseEntity<List<ItemResponseDto>> filterProductsForBirdsItems(
			@RequestBody ProductsForBirdsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/handTools/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Hand Tools Items")
	public ResponseEntity<List<ItemResponseDto>> filterHandToolsItems(
			@RequestBody HandToolsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/machineTools/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Machine Tools Items")
	public ResponseEntity<List<ItemResponseDto>> filterMachineToolsItems(
			@RequestBody MachineToolsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/electricalDevices/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Electrical Devices Items")
	public ResponseEntity<List<ItemResponseDto>> filterElectricalDevicesItems(
			@RequestBody ElectricalDevicesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/measuringEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Measuring Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterMeasuringEquipmentItems(
			@RequestBody MeasuringEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/saws/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Saws Items")
	public ResponseEntity<List<ItemResponseDto>> filterSawsItems(
			@RequestBody SawsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/weldingEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Welding Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterWeldingEquipmentItems(
			@RequestBody WeldingEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/concreteMixers/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Concrete Mixers Items")
	public ResponseEntity<List<ItemResponseDto>> filterConcreteMixersItems(
			@RequestBody ConcreteMixersFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/laddersAndStepladders/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Ladders and Stepladders Items")
	public ResponseEntity<List<ItemResponseDto>> filterLaddersAndStepladdersItems(
			@RequestBody LaddersAndStepladdersFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/scaffoldingAndTowers/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Scaffolding and Towers Items")
	public ResponseEntity<List<ItemResponseDto>> filterScaffoldingAndTowersItems(
			@RequestBody ScaffoldingAndTowersFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/protectiveEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Protective Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterProtectiveEquipmentItems(
			@RequestBody ProtectiveEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/gardeningEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Gardening Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterGardeningEquipmentItems(
			@RequestBody GardeningEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/faucets/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Faucets Items")
	public ResponseEntity<List<ItemResponseDto>> filterFaucetsItems(
			@RequestBody FaucetsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/showersAndBaths/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Showers and Baths Items")
	public ResponseEntity<List<ItemResponseDto>> filterShowersAndBathsItems(
			@RequestBody ShowersAndBathsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/sinksAndWashbasins/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Sinks and Washbasins Items")
	public ResponseEntity<List<ItemResponseDto>> filterSinksAndWashbasinsItems(
			@RequestBody SinksAndWashbasinsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/pumps/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Pumps Items")
	public ResponseEntity<List<ItemResponseDto>> filterPumpsItems(
			@RequestBody PumpsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/waterMeters/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Water Meters Items")
	public ResponseEntity<List<ItemResponseDto>> filterWaterMetersItems(
			@RequestBody WaterMetersFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/toiletsAndBidets/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Toilets and Bidets Items")
	public ResponseEntity<List<ItemResponseDto>> filterToiletsAndBidetsItems(
			@RequestBody ToiletsAndBidetsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/windows/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Windows Items")
	public ResponseEntity<List<ItemResponseDto>> filterWindowsItems(
			@RequestBody WindowsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/doors/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Doors Items")
	public ResponseEntity<List<ItemResponseDto>> filterDoorsItems(
			@RequestBody DoorsFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/gatesAndFences/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Gates and Fences Items")
	public ResponseEntity<List<ItemResponseDto>> filterGatesAndFencesItems(
			@RequestBody GatesAndFencesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/flooring/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Flooring Items")
	public ResponseEntity<List<ItemResponseDto>> filterFlooringItems(
			@RequestBody FlooringFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/waterSupplyAndPipes/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Water Supply and Pipes Items")
	public ResponseEntity<List<ItemResponseDto>> filterWaterSupplyAndPipesItems(
			@RequestBody WaterSupplyAndPipesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/retailAndShopsEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Retail and Shops Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterRetailAndShopsEquipmentItems(
			@RequestBody RetailAndShopsEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/officeEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Office Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterOfficeEquipmentItems(
			@RequestBody OfficeEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/manufacturingEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Manufacturing Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterManufacturingEquipmentItems(
			@RequestBody ManufacturingEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/restaurantsAndCafesEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Restaurants and Cafes Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterRestaurantsAndCafesEquipmentItems(
			@RequestBody RestaurantsAndCafesEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/beautySalonsEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Beauty Salons Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterBeautySalonsEquipmentItems(
			@RequestBody BeautySalonsEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/carServicesEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Car Services Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterCarServicesEquipmentItems(
			@RequestBody CarServicesEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/agriculturalEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Agricultural Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterAgriculturalEquipmentItems(
			@RequestBody AgriculturalEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/warehouseEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Warehouse Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterWarehouseEquipmentItems(
			@RequestBody WarehouseEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/attractionsAndVendingMachines/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Attractions and Vending Machines Items")
	public ResponseEntity<List<ItemResponseDto>> filterAttractionsAndVendingMachinesItems(
			@RequestBody AttractionsAndVendingMachinesFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

	@PostMapping("/advertisingAndExhibitionEquipment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Advertising and Exhibition Equipment Items")
	public ResponseEntity<List<ItemResponseDto>> filterAdvertisingAndExhibitionEquipmentItems(
			@RequestBody AdvertisingAndExhibitionEquipmentFilterDto filterDto) {
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemService.filterItems(filterDto)));
	}

}