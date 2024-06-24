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
	@Operation(summary = "Create item")
	public ResponseEntity<ItemResponseDto> createItem(@PathVariable Long categoryId,
			@RequestPart(value = "dto") ItemRequestDto itemRequestDto,
			@RequestParam(value = "images") List<MultipartFile> images) {
		return ResponseEntity.ok(itemService.save(itemRequestDto, images, categoryId));
	}

	@GetMapping("/category/{categoryId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Get item by category id")
	public ResponseEntity<List<ItemResponseDto>> getItemsByCategoryId(@PathVariable Long categoryId) {
		return ResponseEntity.ok(itemService.findItemsByCategory(categoryId));
	}

	@PutMapping("/{categoryId}/item/{itemId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Update item")
	public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long categoryId,@PathVariable Long itemId,
			@RequestBody ItemRequestDto itemRequestDto) {
		return ResponseEntity.ok(itemService.update(itemRequestDto, categoryId, itemId));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get item by id")
	public ResponseEntity<ItemResponseDto> getItemById(@PathVariable Long id) {
		return ResponseEntity.ok(itemService.findById(id));
	}

	@GetMapping
	@Operation(summary = "Get all items")
	public ResponseEntity<Page<ItemResponseDto>> getAllItem(@RequestParam(required = false) Integer page,
															@RequestParam(required = false) Integer size) {
		PageRequest pageRequest = CustomPageRequest.from(page, size, Sort.unsorted());
		return ResponseEntity.ok(itemService.findAllItems(pageRequest));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Delete item by id")
	public ResponseEntity<List<ItemResponseDto>> deleteItemById(@PathVariable Long id) {
		itemService.deleteById(id);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/search")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Search items")
	public ResponseEntity<List<ItemResponseDto>> searchItem(@RequestBody SearchDto searchDto) {
		return ResponseEntity.ok(itemService.searchItems(searchDto));
	}

	@PostMapping("/auto/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter auto items")
	public ResponseEntity<List<ItemResponseDto>> filterAutoItems(@RequestBody AutoFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/truck/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter truck items")
	public ResponseEntity<List<ItemResponseDto>> filterTruckItems(@RequestBody TruckFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/bus/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter bus items")
	public ResponseEntity<List<ItemResponseDto>> filterBusItems(@RequestBody BusFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/apartmentBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter apartment buy items")
	public ResponseEntity<List<ItemResponseDto>> filterApartmentBuyItems(@RequestBody ApartmentBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/apartmentRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter apartment rental items")
	public ResponseEntity<List<ItemResponseDto>> filterApartmentRentalItems(@RequestBody ApartmentRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/houseBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter house buy items")
	public ResponseEntity<List<ItemResponseDto>> filterHouseBuyItems(@RequestBody HouseBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/houseRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter house rental items")
	public ResponseEntity<List<ItemResponseDto>> filterHouseRentalItems(@RequestBody HouseRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/commercialBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter commercial buy items")
	public ResponseEntity<List<ItemResponseDto>> filterCommercialBuyItems(@RequestBody CommercialBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/commercialRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter commercial rental items")
	public ResponseEntity<List<ItemResponseDto>> filterCommercialRentalItems(@RequestBody CommercialRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/garageAndParkingBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter garage and parking buy items")
	public ResponseEntity<List<ItemResponseDto>> filterGarageAndParkingBuyItems(@RequestBody GarageAndParkingBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/garageAndParkingRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter garage and parking rental items")
	public ResponseEntity<List<ItemResponseDto>> filterGarageAndParkingRentalItems(@RequestBody GarageAndParkingRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/landBuy/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter land buy items")
	public ResponseEntity<List<ItemResponseDto>> filterLandBuyItems(@RequestBody LandBuyFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/landRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter land rental items")
	public ResponseEntity<List<ItemResponseDto>> filterLandRentalItems(@RequestBody LandRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/newConstructionApartment/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter new construction apartment items")
	public ResponseEntity<List<ItemResponseDto>> filterNewConstructionApartmentItems(@RequestBody NewConstructionApartmentFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/newConstructionHouse/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter new construction house items")
	public ResponseEntity<List<ItemResponseDto>> filterNewConstructionHouseItems(@RequestBody NewConstructionHouseFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/apartmentDailyRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter apartment daily rental items")
	public ResponseEntity<List<ItemResponseDto>> filterApartmentDailyRentalItems(@RequestBody ApartmentDailyRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/houseDailyRental/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter house daily rental items")
	public ResponseEntity<List<ItemResponseDto>> filterHouseDailyRentalItems(@RequestBody HouseDailyRentalFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/mobile/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter mobile phone items")
	public ResponseEntity<List<ItemResponseDto>> filterMobilePhoneItems(@RequestBody MobilePhoneFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/notebook/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter notebook items")
	public ResponseEntity<List<ItemResponseDto>> filterNotebookItems(@RequestBody NotebookFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/computer/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter computer items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerItems(@RequestBody ComputerFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/smartWatch/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter smart watch items")
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
	@Operation(summary = "Filter TV items")
	public ResponseEntity<List<ItemResponseDto>> filterTVItems(@RequestBody TVFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/gamingConsole/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter gaming console items")
	public ResponseEntity<List<ItemResponseDto>> filterGamingConsoleItems(@RequestBody GamingConsoleFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/headphone/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter headphone items")
	public ResponseEntity<List<ItemResponseDto>> filterHeadphoneItems(@RequestBody HeadphoneFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/computerAndNotebookParts/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter computer and notebook parts items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerAndNotebookPartsItems(@RequestBody ComputerAndNotebookPartsFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/photoAndVideoCamera/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter photo and video camera items")
	public ResponseEntity<List<ItemResponseDto>> filterPhotoAndVideoCameraItems(@RequestBody PhotoAndVideoCameraFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/computerGames/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter computer games items")
	public ResponseEntity<List<ItemResponseDto>> filterComputerGamesItems(@RequestBody ComputerGamesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/smartHomeAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter smart home accessories items")
	public ResponseEntity<List<ItemResponseDto>> filterSmartHomeAccessoriesItems(@RequestBody SmartHomeAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/washer/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter washer items")
	public ResponseEntity<List<ItemResponseDto>> filterWasherItems(@RequestBody WasherFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/clothesDryer/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter clothes dryer items")
	public ResponseEntity<List<ItemResponseDto>> filterClothesDryerItems(@RequestBody ClothesDryerFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}

	@PostMapping("/ironAndAccessories/filter")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Filter iron and accessories items")
	public ResponseEntity<List<ItemResponseDto>> filterIronAndAccessoriesItems(@RequestBody IronAndAccessoriesFilterDto filterDto) {
		return ResponseEntity.ok(itemService.filterItems(filterDto));
	}


}