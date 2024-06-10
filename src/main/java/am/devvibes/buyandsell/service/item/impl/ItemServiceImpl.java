package am.devvibes.buyandsell.service.item.impl;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.filter.FilterAndSearchDto;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.FieldEntity;
import am.devvibes.buyandsell.entity.FieldNameEntity;
import am.devvibes.buyandsell.entity.ItemEntity;
import am.devvibes.buyandsell.entity.Location;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.repository.ItemRepository;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.value.ValueService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import am.devvibes.buyandsell.util.LocationEnum;
import am.devvibes.buyandsell.util.Status;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {


	private final ItemRepository itemRepository;
	private final ItemMapper itemMapper;
	private final SecurityService securityService;
	private final ValueService valueService;

	@Override
	@Transactional
	public ItemResponseDto save(ItemRequestDto itemRequestDto, Long categoryId) {
		ItemEntity itemEntity = itemMapper.mapDtoToEntity(itemRequestDto, categoryId);
		return itemMapper.mapEntityToDto(itemRepository.save(itemEntity));
	}

	@Override
	@Transactional
	public ItemResponseDto findById(Long id) {
		ItemEntity itemEntity = getItemByIdOrElseThrow(id);
		return itemMapper.mapEntityToDto(itemEntity);
	}

	@Override
	@Transactional
	public Page<ItemResponseDto> findAllItems(PageRequest pageRequest) {
		return itemRepository.findAll(pageRequest).map(itemMapper::mapEntityToDto);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		String currentUserId = securityService.getCurrentUserId();
		ItemEntity itemEntity = getItemByIdOrElseThrow(id);

		if (!itemEntity.getUserEntity().getId().equals(currentUserId)) {
			throw new SomethingWentWrongException(ExceptionConstants.INVALID_ACTION);
		}
		itemEntity.setStatus(Status.DELETED);
		itemRepository.save(itemEntity);
	}

	@Override
	@Transactional
	public ItemResponseDto update(ItemRequestDto itemRequestDto, Long categoryId, Long itemId) {
		ItemEntity itemEntity = getItemByIdOrElseThrow(itemId);
		return itemMapper.mapEntityToDto(updateEntity(itemEntity, itemRequestDto));
	}

	@Override
	public List<ItemResponseDto> searchItems(FilterAndSearchDto.SearchDto searchDto) {
		Specification<ItemEntity> specification = Specification.where((root, criteriaQuery, criteriaBuilder) -> {
			var predicates = new ArrayList<Predicate>();

			if (nonNull(searchDto.getStroke())) {
				Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + searchDto.getStroke().toLowerCase() + "%");
				predicates.add(nameLike);

				Predicate lastNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("field_name").get("Year")), "%" + searchDto.getStroke().toLowerCase() + "%");
				predicates.add(lastNameLike);
			}
			return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
		});
		return itemMapper.mapEntityListToDtoList(itemRepository.findAll(specification));
	}

	@Override
	public List<ItemResponseDto> filterItems(FilterAndSearchDto.FilterDto filterDto) {
		Specification<ItemEntity> specification = Specification.where((root, criteriaQuery, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			Subquery<String> subquery = criteriaQuery.subquery(String.class);
			subquery.from(FieldNameEntity.class);

			Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);
			Join<ItemEntity, FieldEntity> fieldJoin = itemRoot.join("fields");
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = fieldJoin.join("fieldName");

			if (nonNull(filterDto.getMark())) {
				subquery.select(fieldNameJoin.get("fieldName"))
						.where(
								criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark")
						).distinct(true);
				predicates.add(criteriaBuilder.equal(subquery.select(fieldNameJoin.get("value")), filterDto.getMark()));
			}
			if (nonNull(filterDto.getStartYear())) {
				subquery.select(fieldNameJoin.get("fieldName"))
						.where(
								criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Year")
						).distinct(true);

				predicates.add(criteriaBuilder.greaterThanOrEqualTo(subquery, filterDto.getStartYear()));
			}
			if (nonNull(filterDto.getEndYear())) {
				subquery.select(fieldNameJoin.get("fieldName"))
						.where(
								criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Year")
						).distinct(true);

				predicates.add(criteriaBuilder.lessThanOrEqualTo(subquery, filterDto.getEndYear()));
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		});
		return itemMapper.mapEntityListToDtoList(itemRepository.findAll(specification));
	}


	private ItemEntity updateEntity(ItemEntity itemEntity, ItemRequestDto itemRequestDto) {

		itemEntity.setTitle(isNull(itemRequestDto.getTitle()) ? itemEntity.getTitle() : itemRequestDto.getTitle());
		itemEntity.setDescription(isNull(itemRequestDto.getDescription()) ? itemEntity.getDescription() :
				itemRequestDto.getDescription());
		itemEntity.setPrice(Price.builder()
				.price(isNull(itemRequestDto.getPrice()) ? itemEntity.getPrice().getPrice() :
						itemRequestDto.getPrice())
				.currency(isNull(itemRequestDto.getCurrency()) ? itemEntity.getPrice().getCurrency() :
						itemRequestDto.getCurrency())
				.build());

		itemEntity.setFields(valueService.updateValues(itemEntity.getFields(), itemRequestDto.getFieldsValue()));

		itemEntity.setDescription((isNull(itemRequestDto.getDescription()) ? itemEntity.getDescription() :
				itemRequestDto.getDescription()));

		itemEntity.setLocation(Location.builder()
				.country(LocationEnum.getCountry(isNull(itemRequestDto.getCityId()) ? itemEntity.getLocation().getCity().getId() : itemRequestDto.getCityId()))
				.city(LocationEnum.getCity(isNull(itemRequestDto.getCityId()) ? itemEntity.getLocation().getCity().getId() : itemRequestDto.getCityId()))
				.region(LocationEnum.getRegion(isNull(itemRequestDto.getCityId()) ? itemEntity.getLocation().getCity().getId() : itemRequestDto.getCityId()))
				.address(isNull(itemRequestDto.getAddress()) ? itemEntity.getLocation().getAddress(): itemRequestDto.getAddress())
				.build());
		itemEntity.setImgUrl(isNull(itemRequestDto.getImgUrl()) ? itemEntity.getImgUrl() : itemRequestDto.getImgUrl());
		itemEntity.setUpdatedAt(ZonedDateTime.now());
		return itemRepository.save(itemEntity);
	}

	private ItemEntity getItemByIdOrElseThrow(Long id) {
		return itemRepository.findById(id).orElseThrow(() -> new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND));
	}

}
