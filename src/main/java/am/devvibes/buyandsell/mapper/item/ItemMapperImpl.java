package am.devvibes.buyandsell.mapper.item;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.location.LocationCityDto;
import am.devvibes.buyandsell.dto.location.LocationCountryDto;
import am.devvibes.buyandsell.dto.location.LocationRegionDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.dto.location.LocationDto;
import am.devvibes.buyandsell.mapper.priceHistory.PriceHistoryMapper;
import am.devvibes.buyandsell.mapper.value.ValueMapper;
import am.devvibes.buyandsell.service.category.CategoryService;
import am.devvibes.buyandsell.service.location.LocationService;
import am.devvibes.buyandsell.service.s3.impl.S3ServiceImpl;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.impl.UserServiceImpl;
import am.devvibes.buyandsell.service.value.ValueService;
import am.devvibes.buyandsell.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ItemMapperImpl implements ItemMapper {

	private final UserServiceImpl userService;
	private final SecurityService securityService;
	private final CategoryService categoryService;
	private final ValueMapper valueMapper;
	private final ValueService valueService;
	private final S3ServiceImpl s3Service;
	private final PriceHistoryMapper priceHistoryMapper;
	private final LocationService locationService;

	@Override
	public ItemEntity mapDtoToEntity(ItemRequestDto itemRequestDto, Long categoryId) {
		return ItemEntity.builder()
				.title(itemRequestDto.getTitle())
				.description(itemRequestDto.getDescription())
				.price(Price.builder()
						.price(itemRequestDto.getPrice())
						.currency(itemRequestDto.getCurrency())
						.build())
				.userEntity(userService.findUserById(securityService.getCurrentUserId()))
				.city(locationService.getCityById(itemRequestDto.getCityId()))
				.address(itemRequestDto.getAddress())
				.lat(itemRequestDto.getLat())
				.lon(itemRequestDto.getLon())
				.imgKeys(itemRequestDto.getImgKeys())
				.category(categoryService.findCategoryEntityOrElseThrow(categoryId))
				.fields(valueService.saveAllValues(itemRequestDto.getFieldsValue()))
				.phoneNumbers(itemRequestDto.getPhoneNumbers())
				.countOfViews(0L)
				.viewedUsersId(new ArrayList<>())
				.status(Status.CREATED)
				.build();
	}

	@Override
	public ItemEntity mapDtoToEntityFromBusiness(ItemRequestDto itemRequestDto,
			BusinessPageEntity businessPageEntity) {
		return ItemEntity.builder()
				.title(itemRequestDto.getTitle())
				.description(itemRequestDto.getDescription())
				.price(Price.builder()
						.price(itemRequestDto.getPrice())
						.currency(itemRequestDto.getCurrency())
						.build())
				.businessPage(businessPageEntity)
				.city(locationService.getCityById(itemRequestDto.getCityId()))
				.address(itemRequestDto.getAddress())
				.lat(itemRequestDto.getLat())
				.lon(itemRequestDto.getLon())
				.imgKeys(itemRequestDto.getImgKeys())
				.category(businessPageEntity.getCategory())
				.fields(valueService.saveAllValues(itemRequestDto.getFieldsValue()))
				.phoneNumbers(itemRequestDto.getPhoneNumbers())
				.countOfViews(0L)
				.viewedUsersId(new ArrayList<>())
				.status(Status.CREATED)
				.build();
	}

	@Override
	public ItemResponseDto mapEntityToDto(ItemEntity itemEntity) {
		return ItemResponseDto.builder()
				.id(itemEntity.getId())
				.title(itemEntity.getTitle())
				.description(itemEntity.getDescription())
				.price(itemEntity.getPrice())
				.fields(valueMapper.mapEntityListToDtoList(itemEntity.getFields()))
				.description(itemEntity.getDescription())
				.businessPageId(Objects.nonNull(itemEntity.getBusinessPage())? itemEntity.getBusinessPage().getId() : null)
				.userId(Objects.nonNull(itemEntity.getUserEntity()) ? itemEntity.getUserEntity().getId() : null )
				.status(itemEntity.getStatus())
				.locationDto(LocationDto.builder()
						.country(LocationCountryDto.builder()
								.country_eng(itemEntity.getCity().getRegion().getCountry().getName_eng())
								.country_ru(itemEntity.getCity().getRegion().getCountry().getName_ru())
								.country_hy(itemEntity.getCity().getRegion().getCountry().getName_hy())
								.build())
						.region(LocationRegionDto.builder()
								.region_eng(itemEntity.getCity().getRegion().getName_eng())
								.region_ru(itemEntity.getCity().getRegion().getName_ru())
								.region_hy(itemEntity.getCity().getRegion().getName_hy())
								.build())
						.city(LocationCityDto.builder()
								.city_eng(itemEntity.getCity().getName_eng())
								.city_ru(itemEntity.getCity().getName_ru())
								.city_hy(itemEntity.getCity().getName_hy())
								.build())
						.address(itemEntity.getAddress())
						.lat(itemEntity.getLat())
						.lon(itemEntity.getLon())
						.build())
				.priceHistory(priceHistoryMapper.mapEntityListToDtoList(itemEntity.getPriceHistories()))
				.imgUrls(s3Service.getImagesPresignedDownloadUrls(itemEntity.getImgKeys()))
				.phoneNumbers(itemEntity.getPhoneNumbers())
				.isViewed(itemEntity.getViewedUsersId().contains(securityService.getCurrentUserId()))
				.countOfViews(itemEntity.getCountOfViews())
				.build();
	}

	@Override
	public List<ItemResponseDto> mapEntityListToDtoList(List<ItemEntity> itemEntityList) {
		if (itemEntityList != null && !itemEntityList.isEmpty()) {
			return itemEntityList.stream().map(this::mapEntityToDto).toList();
		}
		return null;
	}

}
