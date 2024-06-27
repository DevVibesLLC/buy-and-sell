package am.devvibes.buyandsell.service.item;

import am.devvibes.buyandsell.dto.filter.*;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.search.SearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {

	ItemResponseDto save(ItemRequestDto itemRequestDto, List<MultipartFile> images, Long categoryId);

	ItemResponseDto findById(Long id);

	Page<ItemResponseDto> findAllItems(PageRequest pageRequest);

	void deleteById(Long id);

	ItemResponseDto update(ItemRequestDto itemRequestDto, Long categoryId, Long itemId);

	List<ItemResponseDto> searchItems(SearchDto searchDto);

	List<ItemResponseDto> filterItems(AutoFilterDto filterDto);

	List<ItemResponseDto> filterItems(TruckFilterDto filterDto);

	List<ItemResponseDto> filterItems(BusFilterDto filterDto);

	List<ItemResponseDto> filterItems(ApartmentBuyFilterDto filterDto);

	List<ItemResponseDto> filterItems(ApartmentRentalFilterDto filterDto);

	List<ItemResponseDto> filterItems(HouseBuyFilterDto filterDto);

	List<ItemResponseDto> filterItems(HouseRentalFilterDto filterDto);

	List<ItemResponseDto> filterItems(CommercialBuyFilterDto filterDto);

	List<ItemResponseDto> filterItems(CommercialRentalFilterDto filterDto);

	List<ItemResponseDto> filterItems(GarageAndParkingBuyFilterDto filterDto);

	List<ItemResponseDto> filterItems(GarageAndParkingRentalFilterDto filterDto);

	List<ItemResponseDto> filterItems(LandBuyFilterDto filterDto);

	List<ItemResponseDto> filterItems(LandRentalFilterDto filterDto);

	List<ItemResponseDto> filterItems(NewConstructionApartmentFilterDto filterDto);

	List<ItemResponseDto> filterItems(NewConstructionHouseFilterDto filterDto);

	List<ItemResponseDto> filterItems(ApartmentDailyRentalFilterDto filterDto);

	List<ItemResponseDto> filterItems(HouseDailyRentalFilterDto filterDto);

	List<ItemResponseDto> filterItems(MobilePhoneFilterDto filterDto);

	List<ItemResponseDto> filterItems(NotebookFilterDto filterDto);

	List<ItemResponseDto> filterItems(ComputerFilterDto filterDto);

	List<ItemResponseDto> filterItems(SmartWatchFilterDto filterDto);

	List<ItemResponseDto> filterItems(TabletFilterDto filterDto);

	List<ItemResponseDto> filterItems(TVFilterDto filterDto);

	List<ItemResponseDto> filterItems(GamingConsoleFilterDto filterDto);

	List<ItemResponseDto> filterItems(HeadphoneFilterDto filterDto);

	List<ItemResponseDto> filterItems(ComputerAndNotebookPartsFilterDto filterDto);

	List<ItemResponseDto> filterItems(PhotoAndVideoCameraFilterDto filterDto);

	List<ItemResponseDto> filterItems(ComputerGamesFilterDto filterDto);

	List<ItemResponseDto> filterItems(SmartHomeAccessoriesFilterDto filterDto);

	List<ItemResponseDto> filterItems(WasherFilterDto filterDto);

	List<ItemResponseDto> filterItems(ClothesDryerFilterDto filterDto);

	List<ItemResponseDto> filterItems(IronAndAccessoriesFilterDto filterDto);

	List<ItemResponseDto> filterItems(RefrigeratorFilterDto filterDto);

	List<ItemResponseDto> filterItems(FreezerFilterDto filterDto);

	List<ItemResponseDto> filterItems(DishwasherFilterDto filterDto);

	List<ItemResponseDto> filterItems(MicrowaveFilterDto filterDto);

	List<ItemResponseDto> filterItems(StoveFilterDto filterDto);

	List<ItemResponseDto> filterItems(CoffeeMakerAndAccessoriesFilterDto filterDto);

	List<ItemResponseDto> filterItems(KettleFilterDto filterDto);

	List<ItemResponseDto> filterItems(RangeHoodFilterDto filterDto);

	List<ItemResponseDto> filterItems(VacuumCleanerFilterDto filterDto);

	List<ItemResponseDto> filterItems(RoboticVacuumFilterDto filterDto);

	List<ItemResponseDto> filterItems(FloorWasherFilterDto filterDto);

	List<ItemResponseDto> filterItems(AirConditionerFilterDto filterDto);

	List<ItemResponseDto> filterItems(WaterHeatersFilterDto filterDto);

	List<ItemResponseDto> filterItems(AirPurifiersAndHumidifiersFilterDto filterDto);

	List<ItemResponseDto> filterItems(ComputerPeripheralFilterDto filterDto);

	List<ItemResponseDto> filterItems(AudioPlayerAndStereoFilterDto filterDto);

	List<ItemResponseDto> filterItems(QuadcoptersAndDronesFilterDto filterDto);

	List<ItemResponseDto> filterItems(SofaAndArmchairFilterDto filterDto);

	List<ItemResponseDto> filterItems(StorageFilterDto filterDto);

	List<ItemResponseDto> filterItems(TableAndChairFilterDto filterDto);

	List<ItemResponseDto> filterItems(BedroomFurnitureFilterDto filterDto);

	List<ItemResponseDto> filterItems(KitchenFurnitureFilterDto filterDto);

	List<ItemResponseDto> filterItems(GardenFurnitureFilterDto filterDto);

	List<ItemResponseDto> filterItems(BarbecueAndAccessoriesFilterDto filterDto);

	List<ItemResponseDto> filterItems(GardenDecorFilterDto filterDto);

	List<ItemResponseDto> filterItems(GardenAccessoriesFilterDto filterDto);

	List<ItemResponseDto> filterItems(LightingFilterDto filterDto);

	List<ItemResponseDto> filterItems(TextileFilterDto filterDto);

	List<ItemResponseDto> findItemsByCategory(Long categoryId);

}
