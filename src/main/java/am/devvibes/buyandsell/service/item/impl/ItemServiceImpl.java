package am.devvibes.buyandsell.service.item.impl;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.filter.FilterDto;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.search.SearchDto;
import am.devvibes.buyandsell.entity.field.FieldEntity;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.location.Location;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.repository.item.ItemRepository;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.value.ValueService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import am.devvibes.buyandsell.util.LocationEnum;
import am.devvibes.buyandsell.util.Status;
import jakarta.persistence.EntityManager;
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
	private final EntityManager entityManager;

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
	public List<ItemResponseDto> searchItems(SearchDto searchDto) {
		Specification<ItemEntity> specification = Specification.where((root, criteriaQuery, criteriaBuilder) -> {
			var predicates = new ArrayList<Predicate>();

			if (nonNull(searchDto.getStroke())) {
				Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + searchDto.getStroke().toLowerCase() + "%");
				predicates.add(nameLike);

				Predicate lastNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + searchDto.getStroke().toLowerCase() + "%");
				predicates.add(lastNameLike);
			}
			return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
		});
		return itemMapper.mapEntityListToDtoList(itemRepository.findAll(specification));
	}

	@Override
	public List<ItemResponseDto> filterItems(FilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getModel()) && !filterDto.getModel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Model"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getModel())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartYear()) && !filterDto.getStartYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Year"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartYear())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndYear()) && !filterDto.getEndYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Year"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndYear())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBodyType()) && !filterDto.getBodyType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Body Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getBodyType())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate = criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate = criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"), filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("location").get("region"), filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("location").get("city"), filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartEngineSize()) && !filterDto.getStartEngineSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Engine Size"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartEngineSize())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndEngineSize()) && !filterDto.getEndEngineSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Engine Size"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndEngineSize())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTransmission()) && !filterDto.getTransmission().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Transmission"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getTransmission())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getDriveType()) && !filterDto.getDriveType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Drive Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getDriveType())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartMileage()) && !filterDto.getStartMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mileage"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartMileage())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndMileage()) && !filterDto.getEndMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mileage"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndMileage())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSteeringWheel()) && !filterDto.getSteeringWheel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Steering Wheel"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getSteeringWheel())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getClearedCustom()) && !filterDto.getClearedCustom().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Cleared Custom"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getClearedCustom())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getColor())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWheelSize()) && !filterDto.getWheelSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Wheel Size"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWheelSize())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getHeadlights()) && !filterDto.getHeadlights().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Headlights"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getHeadlights())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getInteriorColor()) && !filterDto.getInteriorColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Interior Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getInteriorColor())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getExteriorColor()) && !filterDto.getExteriorColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Exterior Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getExteriorColor())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSunroof()) && !filterDto.getSunroof().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Sunroof"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getSunroof())
			);
			predicates.add(predicate);
		}


		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);

	}

	@Override
	public List<ItemResponseDto> findItemsByCategory(Long categoryId) {
		List<ItemEntity> itemsByCategoryId = itemRepository.findByCategoryId(categoryId);
		return itemMapper.mapEntityListToDtoList(itemsByCategoryId);
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
