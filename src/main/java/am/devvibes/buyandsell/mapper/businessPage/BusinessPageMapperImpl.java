package am.devvibes.buyandsell.mapper.businessPage;

import am.devvibes.buyandsell.dto.businessPage.BusinessPageRequestDto;
import am.devvibes.buyandsell.dto.businessPage.BusinessPageResponseDto;
import am.devvibes.buyandsell.dto.location.LocationCityDto;
import am.devvibes.buyandsell.dto.location.LocationCountryDto;
import am.devvibes.buyandsell.dto.location.LocationRegionDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.dto.location.LocationDto;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.service.category.CategoryService;
import am.devvibes.buyandsell.service.location.LocationService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessPageMapperImpl implements BusinessPageMapper{

	private final UserService userService;
	private final SecurityService securityService;
	private final CategoryService categoryService;
	private final LocationService locationService;
	private final ItemMapper itemMapper;

	@Override
	public BusinessPageResponseDto mapEntityToDto(BusinessPageEntity businessPageEntity) {
		return BusinessPageResponseDto.builder()
				.id(businessPageEntity.getId())
				.ownerId(businessPageEntity.getOwner().getId())
				.adds(itemMapper.mapEntityListToDtoList(businessPageEntity.getAdds()))
				.title(businessPageEntity.getTitle())
				.description(businessPageEntity.getDescription())
				.email(businessPageEntity.getEmail())
				.phoneNumbers(businessPageEntity.getPhoneNumbers())
				.logoKey(businessPageEntity.getLogoKey())
				.bannerKey(businessPageEntity.getBannerKey())
				.workingDaysAndHours(businessPageEntity.getWorkingDaysAndHours())
				.socialMediaLinks(businessPageEntity.getSocialMediaLinks())
				.locationDto(LocationDto.builder()
						.country(LocationCountryDto.builder()
								.country_eng(businessPageEntity.getCity().getRegion().getCountry().getName_eng())
								.country_ru(businessPageEntity.getCity().getRegion().getCountry().getName_ru())
								.country_hy(businessPageEntity.getCity().getRegion().getCountry().getName_hy())
								.build())
						.region(LocationRegionDto.builder()
								.region_eng(businessPageEntity.getCity().getRegion().getName_eng())
								.region_ru(businessPageEntity.getCity().getRegion().getName_ru())
								.region_hy(businessPageEntity.getCity().getRegion().getName_hy())
								.build())
						.city(LocationCityDto.builder()
								.city_eng(businessPageEntity.getCity().getName_eng())
								.city_ru(businessPageEntity.getCity().getName_ru())
								.city_hy(businessPageEntity.getCity().getName_hy())
								.build())
						.address(businessPageEntity.getAddress())
						.lat(businessPageEntity.getLat())
						.lon(businessPageEntity.getLon())
						.build())
				.categoryId(businessPageEntity.getCategory().getId())
				.build();
	}

	@Override
	public BusinessPageEntity mapDtoToEntity(BusinessPageRequestDto businessPageRequestDto, Long categoryId) {
		return BusinessPageEntity.builder()
				.owner(userService.findUserById(securityService.getCurrentUserId()))
				.workingDaysAndHours(businessPageRequestDto.getWorkingDaysAndHours())
				.socialMediaLinks(businessPageRequestDto.getSocialMediaLinks())
				.logoKey(businessPageRequestDto.getLogoKey())
				.bannerKey(businessPageRequestDto.getBannerKey())
				.adds(null)
				.title(businessPageRequestDto.getTitle())
				.description(businessPageRequestDto.getDescription())
				.email(businessPageRequestDto.getEmail())
				.phoneNumbers(businessPageRequestDto.getPhoneNumbers())
				.city(locationService.getCityById(businessPageRequestDto.getCityId()))
				.address(businessPageRequestDto.getAddress())
				.lat(businessPageRequestDto.getLat())
				.lon(businessPageRequestDto.getLon())
				.category(categoryService.findCategoryEntityOrElseThrow(categoryId))
				.build();
	}

}
