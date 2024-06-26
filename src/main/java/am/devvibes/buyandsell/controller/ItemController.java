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
	public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long categoryId,@PathVariable Long itemId,
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
	public ResponseEntity<List<ItemResponseDto>> filterApartmentRentalItems(@RequestBody ApartmentRentalFilterDto filterDto) {
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
	public ResponseEntity<List<ItemResponseDto>> filterCommercialBuyItems(@RequestBody CommercialBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/commercialRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Commercial Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterCommercialRentalItems(@RequestBody CommercialRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/garageAndParkingBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garage and Parking Buy Items")
	public ResponseEntity<List<ItemResponseDto>> filterGarageAndParkingBuyItems(@RequestBody GarageAndParkingBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/garageAndParkingRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Garage and Parking Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterGarageAndParkingRentalItems(@RequestBody GarageAndParkingRentalFilterDto filterDto) {
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
	public ResponseEntity<List<ItemResponseDto>> filterNewConstructionApartmentItems(@RequestBody NewConstructionApartmentFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/newConstructionHouse/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter New Construction House Items")
	public ResponseEntity<List<ItemResponseDto>> filterNewConstructionHouseItems(@RequestBody NewConstructionHouseFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/apartmentDailyRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Apartment Daily Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterApartmentDailyRentalItems(@RequestBody ApartmentDailyRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/houseDailyRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter House Daily Rental Items")
	public ResponseEntity<List<ItemResponseDto>> filterHouseDailyRentalItems(@RequestBody HouseDailyRentalFilterDto filterDto) {
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
	public ResponseEntity<List<ItemResponseDto>> filterGamingConsoleItems(@RequestBody GamingConsoleFilterDto filterDto) {
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
	public ResponseEntity<List<ItemResponseDto>> filterComputerAndNotebookPartsItems(@RequestBody ComputerAndNotebookPartsFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/photoAndVideoCamera/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Photo and Video Camera Items")
	public ResponseEntity<List<ItemResponseDto>> filterPhotoAndVideoCameraItems(@RequestBody PhotoAndVideoCameraFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/computerGames/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Computer Games Items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerGamesItems(@RequestBody ComputerGamesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/smartHomeAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter Smart Home Accessories Items")
	public ResponseEntity<List<ItemResponseDto>> filterSmartHomeAccessoriesItems(@RequestBody SmartHomeAccessoriesFilterDto filterDto) {
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
	public ResponseEntity<List<ItemResponseDto>> filterIronAndAccessoriesItems(@RequestBody IronAndAccessoriesFilterDto filterDto) {
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
	public ResponseEntity<List<ItemResponseDto>> filterCoffeeMakerAndAccessoriesItems(@RequestBody CoffeeMakerAndAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}
}