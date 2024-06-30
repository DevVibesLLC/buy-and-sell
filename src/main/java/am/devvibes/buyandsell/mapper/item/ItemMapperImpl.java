package am.devvibes.buyandsell.mapper.item;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.location.Location;
import am.devvibes.buyandsell.mapper.value.ValueMapper;
import am.devvibes.buyandsell.service.category.CategoryService;
import am.devvibes.buyandsell.service.s3.impl.S3ServiceImpl;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.impl.UserServiceImpl;
import am.devvibes.buyandsell.service.value.ValueService;
import am.devvibes.buyandsell.util.LocationEnum;
import am.devvibes.buyandsell.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemMapperImpl implements ItemMapper {

	private final UserServiceImpl userService;
	private final SecurityService securityService;
	private final CategoryService categoryService;
	private final ValueMapper valueMapper;
	private final ValueService valueService;
	private final S3ServiceImpl s3Service;

	@Override
	public ItemEntity mapDtoToEntity(ItemRequestDto itemRequestDto, List<MultipartFile> images, Long categoryId) {
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
				.fields(valueService.saveAllValues(itemRequestDto.getFieldsValue()))
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
				.userId(itemEntity.getUserEntity().getId())
				.status(itemEntity.getStatus())
				.location(itemEntity.getLocation())
				.imgUrls(itemEntity.getImgUrls())
				.build();
	}

	@Override
	public List<ItemResponseDto> mapEntityListToDtoList(List<ItemEntity> itemEntityList) {
		return itemEntityList.stream().map(this::mapEntityToDto).toList();
	}

}
