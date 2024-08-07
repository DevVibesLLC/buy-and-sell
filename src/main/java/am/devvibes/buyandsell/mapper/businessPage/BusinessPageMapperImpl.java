package am.devvibes.buyandsell.mapper.businessPage;

import am.devvibes.buyandsell.dto.businessPage.BusinessPageRequestDto;
import am.devvibes.buyandsell.dto.businessPage.BusinessPageResponseDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.location.Location;
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
				.location(Location.builder()
						.country(businessPageEntity.getCity().getRegion().getCountry().getName())
						.region(businessPageEntity.getCity().getRegion().getName())
						.city(businessPageEntity.getCity().getName())
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
