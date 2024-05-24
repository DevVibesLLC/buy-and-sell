package am.devvibes.buyandsell.mapper.impl;

import am.devvibes.buyandsell.classes.Price;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.CategoryEntity;
import am.devvibes.buyandsell.entity.ItemEntity;
import am.devvibes.buyandsell.entity.Location;
import am.devvibes.buyandsell.mapper.ItemMapper;
import am.devvibes.buyandsell.service.category.CategoryService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.impl.UserServiceImpl;
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
	private final UserMapperImpl userMapper;

	@Override
	public ItemEntity mapDtoToEntity(ItemRequestDto itemRequestDto) {
		return ItemEntity.builder()
				.name(itemRequestDto.getName())
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
				.category(categoryService)
				.imgUrl(itemRequestDto.getImgUrl())
				.build();
	}

	@Override
	public ItemResponseDto mapEntityToDto(ItemEntity itemEntity) {
		return ItemResponseDto.builder()
				.name(itemEntity.getName())
				.description(itemEntity.getDescription())
				.price(itemEntity.getPrice())
				.category(itemEntity.getCategory())
				.description(itemEntity.getDescription())
				.user(userMapper.toDto(itemEntity.getUserEntity()))
				.location(itemEntity.getLocation())
				.imgUrl(itemEntity.getImgUrl())
				.build();
	}

	@Override
	public List<ItemResponseDto> mapEntityListToDtoList(List<ItemEntity> itemEntityList) {
		return itemEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
