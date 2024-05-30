package am.devvibes.buyandsell.mapper.item;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.ItemEntity;
import am.devvibes.buyandsell.entity.Location;
import am.devvibes.buyandsell.mapper.value.ValueMapper;
import am.devvibes.buyandsell.service.category.CategoryService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.impl.UserServiceImpl;
import am.devvibes.buyandsell.service.value.ValueService;
import am.devvibes.buyandsell.util.LocationEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemMapperImpl implements ItemMapper {

	private final UserServiceImpl userService;
	private final SecurityService securityService;
	private final CategoryService categoryService;
	private final ValueMapper valueMapper;
	private final ValueService valueService;

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
				.location(Location.builder()
						.country(LocationEnum.getCountry(itemRequestDto.getCityId()))
						.region(LocationEnum.getRegion(itemRequestDto.getCityId()))
						.city(LocationEnum.getCity(itemRequestDto.getCityId()))
						.address(itemRequestDto.getAddress())
						.build())
				.category(categoryService.FindCategoryEntityOrElseThrow(categoryId))
				.values(valueService.saveAllValues(itemRequestDto.getFieldsValue()))
				.imgUrl(itemRequestDto.getImgUrl())
				.build();
	}

	@Override
	public ItemResponseDto mapEntityToDto(ItemEntity itemEntity) {
		return ItemResponseDto.builder()
				.title(itemEntity.getTitle())
				.description(itemEntity.getDescription())
				.price(itemEntity.getPrice())
				.fields(valueMapper.mapEntityListToDtoList(itemEntity.getValues()))
				.description(itemEntity.getDescription())
				.userId(itemEntity.getUserEntity().getId())
				.location(itemEntity.getLocation())
				.imgUrl(itemEntity.getImgUrl())
				.build();
	}

	@Override
	public List<ItemResponseDto> mapEntityListToDtoList(List<ItemEntity> itemEntityList) {
		return itemEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
