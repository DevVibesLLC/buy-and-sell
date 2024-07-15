package am.devvibes.buyandsell.service.item;

import am.devvibes.buyandsell.dto.filter.*;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.priceStatistic.PriceStatisticsRequestDto;
import am.devvibes.buyandsell.dto.search.SearchDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ItemService {

	ItemEntity save(ItemRequestDto itemRequestDto, Long categoryId);

	ItemEntity saveFromBusiness(ItemRequestDto itemRequestDto, BusinessPageEntity businessPageEntity);

	ItemEntity findById(Long id);

	ItemEntity findEntityById(Long id);

	Page<ItemResponseDto> findAllItems(PageRequest pageRequest);

	void deleteById(Long id);

	ItemEntity update(ItemRequestDto itemRequestDto, Long categoryId, Long itemId);

	List<ItemEntity> searchItems(SearchDto searchDto);

	List<ItemEntity> filterItems(AutoFilterDto filterDto);

	List<ItemEntity> filterItems(TruckFilterDto filterDto);

	List<ItemEntity> filterItems(BusFilterDto filterDto);

	List<ItemEntity> filterItems(ApartmentBuyFilterDto filterDto);

	List<ItemEntity> filterItems(ApartmentRentalFilterDto filterDto);

	List<ItemEntity> filterItems(HouseBuyFilterDto filterDto);

	List<ItemEntity> filterItems(HouseRentalFilterDto filterDto);

	List<ItemEntity> filterItems(CommercialBuyFilterDto filterDto);

	List<ItemEntity> filterItems(CommercialRentalFilterDto filterDto);

	List<ItemEntity> filterItems(GarageAndParkingBuyFilterDto filterDto);

	List<ItemEntity> filterItems(GarageAndParkingRentalFilterDto filterDto);

	List<ItemEntity> filterItems(LandBuyFilterDto filterDto);

	List<ItemEntity> filterItems(LandRentalFilterDto filterDto);

	List<ItemEntity> filterItems(NewConstructionApartmentFilterDto filterDto);

	List<ItemEntity> filterItems(NewConstructionHouseFilterDto filterDto);

	List<ItemEntity> filterItems(ApartmentDailyRentalFilterDto filterDto);

	List<ItemEntity> filterItems(HouseDailyRentalFilterDto filterDto);

	List<ItemEntity> filterItems(MobilePhoneFilterDto filterDto);

	List<ItemEntity> filterItems(NotebookFilterDto filterDto);

	List<ItemEntity> filterItems(ComputerFilterDto filterDto);

	List<ItemEntity> filterItems(SmartWatchFilterDto filterDto);

	List<ItemEntity> filterItems(TabletFilterDto filterDto);

	List<ItemEntity> filterItems(TVFilterDto filterDto);

	List<ItemEntity> filterItems(GamingConsoleFilterDto filterDto);

	List<ItemEntity> filterItems(HeadphoneFilterDto filterDto);

	List<ItemEntity> filterItems(ComputerAndNotebookPartsFilterDto filterDto);

	List<ItemEntity> filterItems(PhotoAndVideoCameraFilterDto filterDto);

	List<ItemEntity> filterItems(ComputerGamesFilterDto filterDto);

	List<ItemEntity> filterItems(SmartHomeAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(WasherFilterDto filterDto);

	List<ItemEntity> filterItems(ClothesDryerFilterDto filterDto);

	List<ItemEntity> filterItems(IronAndAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(RefrigeratorFilterDto filterDto);

	List<ItemEntity> filterItems(FreezerFilterDto filterDto);

	List<ItemEntity> filterItems(DishwasherFilterDto filterDto);

	List<ItemEntity> filterItems(MicrowaveFilterDto filterDto);

	List<ItemEntity> filterItems(StoveFilterDto filterDto);

	List<ItemEntity> filterItems(CoffeeMakerAndAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(KettleFilterDto filterDto);

	List<ItemEntity> filterItems(RangeHoodFilterDto filterDto);

	List<ItemEntity> filterItems(VacuumCleanerFilterDto filterDto);

	List<ItemEntity> filterItems(RoboticVacuumFilterDto filterDto);

	List<ItemEntity> filterItems(FloorWasherFilterDto filterDto);

	List<ItemEntity> filterItems(AirConditionerFilterDto filterDto);

	List<ItemEntity> filterItems(WaterHeatersFilterDto filterDto);

	List<ItemEntity> filterItems(AirPurifiersAndHumidifiersFilterDto filterDto);

	List<ItemEntity> filterItems(ComputerPeripheralFilterDto filterDto);

	List<ItemEntity> filterItems(AudioPlayerAndStereoFilterDto filterDto);

	List<ItemEntity> filterItems(QuadcoptersAndDronesFilterDto filterDto);

	List<ItemEntity> filterItems(SofaAndArmchairFilterDto filterDto);

	List<ItemEntity> filterItems(StorageFilterDto filterDto);

	List<ItemEntity> filterItems(TableAndChairFilterDto filterDto);

	List<ItemEntity> filterItems(BedroomFurnitureFilterDto filterDto);

	List<ItemEntity> filterItems(KitchenFurnitureFilterDto filterDto);

	List<ItemEntity> filterItems(GardenFurnitureFilterDto filterDto);

	List<ItemEntity> filterItems(BarbecueAndAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(GardenDecorFilterDto filterDto);

	List<ItemEntity> filterItems(GardenAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(LightingFilterDto filterDto);

	List<ItemEntity> filterItems(TextileFilterDto filterDto);

	List<ItemEntity> filterItems(RugFilterDto filterDto);

	List<ItemEntity> filterItems(InteriorDecorationFilterDto filterDto);

	List<ItemEntity> filterItems(TablewareFilterDto filterDto);

	List<ItemEntity> filterItems(CookingAndBakingFilterDto filterDto);

	List<ItemEntity> filterItems(KitchenAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(BathroomAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(VideoSurveillanceFilterDto filterDto);

	List<ItemEntity> filterItems(CarPartFilterDto filterDto);

	List<ItemEntity> filterItems(WheelAndTireFilterDto filterDto);

	List<ItemEntity> filterItems(RimAndHubCapFilterDto filterDto);

	List<ItemEntity> filterItems(CarBatteryFilterDto filterDto);

	List<ItemEntity> filterItems(GasEquipmentFilterDto filterDto);

	List<ItemEntity> filterItems(OilAndChemicalFilterDto filterDto);

	List<ItemEntity> filterItems(CarAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(CarElectronicFilterDto filterDto);

	List<ItemEntity> filterItems(CarAudioAndVideoFilterDto filterDto);

	List<ItemEntity> filterItems(PersonalTransportationFilterDto filterDto);

	List<ItemEntity> filterItems(AtvAndSnowmobileFilterDto filterDto);

	List<ItemEntity> filterItems(BoatAndWaterTransportFilterDto filterDto);

	List<ItemEntity> filterItems(TrailerAndBoothFilterDto filterDto);

	List<ItemEntity> filterItems(EventVenueRentalFilterDto filterDto);

	List<ItemEntity> filterItems(WomenClothingBuyFilterDto filterDto);

	List<ItemEntity> filterItems(WomenClothingRentalFilterDto filterDto);

	List<ItemEntity> filterItems(WomenShoesFilterDto filterDto);

	List<ItemEntity> filterItems(WomenAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(MenClothingBuyFilterDto filterDto);

	List<ItemEntity> filterItems(MenShoesFilterDto filterDto);

	List<ItemEntity> filterItems(MenAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(JewelleryFilterDto filterDto);

	List<ItemEntity> filterItems(GlassesAndFramesFilterDto filterDto);

	List<ItemEntity> filterItems(WatchesFilterDto filterDto);

	List<ItemEntity> filterItems(HandbagsAndWalletsFilterDto filterDto);

	List<ItemEntity> filterItems(WorkwearAndAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(CarnivalCostumesFilterDto filterDto);

	List<ItemEntity> filterItems(WeddingDressesFilterDto filterDto);

	List<ItemEntity> filterItems(WeddingShoesFilterDto filterDto);

	List<ItemEntity> filterItems(WeddingAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(CollectibleItemsFilterDto filterDto);

	List<ItemEntity> filterItems(PaintingsAndPicturesFilterDto filterDto);

	List<ItemEntity> filterItems(ArtsObjectsFilterDto filterDto);

	List<ItemEntity> filterItems(ArtsAndCraftsFilterDto filterDto);

	List<ItemEntity> filterItems(MotorcycleFilterDto filterDto);

	List<ItemEntity> filterItems(MotorcyclePartsAndAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(GuitarsFilterDto filterDto);

	List<ItemEntity> filterItems(PianosAndKeyboardInstrumentsFilterDto filterDto);

	List<ItemEntity> filterItems(BrassAndWoodwindInstrumentsFilterDto filterDto);

	List<ItemEntity> filterItems(StringInstrumentsFilterDto filterDto);

	List<ItemEntity> filterItems(AccordionsFilterDto filterDto);

	List<ItemEntity> filterItems(DrumsAndPercussionInstrumentsFilterDto filterDto);

	List<ItemEntity> filterItems(StudioAccessoriesFilterDto filterDto);

	List<ItemEntity> filterItems(HuntingAndFishingFilterDto filterDto);

	List<ItemEntity> filterItems(CampingEquipmentFilterDto filterDto);

	List<ItemEntity> filterItems(FitnessAndExerciseEquipmentFilterDto filterDto);

	List<ItemEntity> filterItems(BilliardAndBowlingFilterDto filterDto);

	List<ItemEntity> filterItems(FootballAndBallGamesFilterDto filterDto);

	List<ItemEntity> filterItems(WaterSportsFilterDto filterDto);

	List<ItemEntity> filterItems(WinterSportsEquipmentFilterDto filterDto);

	List<ItemEntity> filterItems(BoxingAndMartialArtsFilterDto filterDto);

	List<ItemEntity> filterItems(TennisAndBadmintonFilterDto filterDto);

	List<ItemEntity> filterItems(MountaineeringFilterDto filterDto);

	List<ItemEntity> filterItems(SportsNutritionFilterDto filterDto);

	List<ItemEntity> filterItems(BooksAndMagazinesFilterDto filterDto);

	List<ItemEntity> filterItems(FilmsAndMusicFilterDto filterDto);

	List<ItemEntity> filterItems(DogsFilterDto filterDto);

	List<ItemEntity> filterItems(CatsFilterDto filterDto);

	List<ItemEntity> filterItems(FishFilterDto filterDto);

	List<ItemEntity> filterItems(BirdsFilterDto filterDto);

	List<ItemEntity> filterItems(RodentsFilterDto filterDto);

	List<ItemEntity> filterItems(PriceStatisticsRequestDto filterDto);

	List<ItemEntity> findItemsByCategory(Long categoryId);

}
