package am.devvibes.buyandsell.service.item.impl;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.filter.*;
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
import am.devvibes.buyandsell.util.CategoryEnum;
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
	public List<ItemResponseDto> filterItems(TruckFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.TRUCK);
		predicates.add(categoryPredicate);

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

		if (nonNull(filterDto.getChassiesConfiguration()) && !filterDto.getChassiesConfiguration().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Chassis configuration"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getChassiesConfiguration())
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

		if (nonNull(filterDto.getSteeringWheel()) && !filterDto.getSteeringWheel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Steering Wheel"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getSteeringWheel())
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

		if (nonNull(filterDto.getEngineType()) && !filterDto.getEngineType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Engine Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getEngineType())
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

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(BusFilterDto filterDto) {

		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.BUS);
		predicates.add(categoryPredicate);

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

		if (nonNull(filterDto.getSteeringWheel()) && !filterDto.getSteeringWheel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Steering Wheel"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getSteeringWheel())
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

		if (nonNull(filterDto.getEngineType()) && !filterDto.getEngineType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Engine Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getEngineType())
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

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);

	}

	@Override
	public List<ItemResponseDto> filterItems(ApartmentBuyFilterDto filterDto) {

		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.APARTMENT_BUY);
		predicates.add(categoryPredicate);

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

		if (nonNull(filterDto.getElevator()) && !filterDto.getElevator().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Elevator"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getElevator())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNewConstruction()) && !filterDto.getNewConstruction().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "New Construction"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNewConstruction())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the Building"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFloorsInTheBuilding())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Appliances"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAppliances())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloor()) && !filterDto.getFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFloor())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBalcony()) && !filterDto.getBalcony().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Balcony"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getBalcony())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCeilingHeight()) && !filterDto.getCeilingHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Ceiling Height"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCeilingHeight())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Furniture"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFurniture())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartFloorArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndFloorArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Renovation"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRenovation())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Parking"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getParking())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWindowViews()) && !filterDto.getWindowViews().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Windows Views"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWindowViews())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTheHouseHas()) && !filterDto.getTheHouseHas().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "The House Has"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getTheHouseHas())
			);
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(ApartmentRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.APARTMENT_RENTAL);
		predicates.add(categoryPredicate);

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

		if (nonNull(filterDto.getElevator()) && !filterDto.getElevator().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Elevator"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getElevator())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNewConstruction()) && !filterDto.getNewConstruction().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "New Construction"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNewConstruction())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the Building"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFloorsInTheBuilding())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Appliances"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAppliances())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Amenities"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAmenities())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloor()) && !filterDto.getFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFloor())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBalcony()) && !filterDto.getBalcony().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Balcony"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getBalcony())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCeilingHeight()) && !filterDto.getCeilingHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Ceiling Height"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCeilingHeight())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Furniture"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFurniture())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartFloorArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndFloorArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Renovation"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRenovation())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Parking"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getParking())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWindowViews()) && !filterDto.getWindowViews().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Windows Views"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWindowViews())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTheHouseHas()) && !filterDto.getTheHouseHas().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "The House Has"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getTheHouseHas())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Pets"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithPets())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithChildren()) && !filterDto.getWithChildren().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Children"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithChildren())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Prepayment"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getPrepayment())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilityPayments()) && !filterDto.getUtilityPayments().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Utility Payments"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getUtilityPayments())
			);
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(HouseBuyFilterDto filterDto) {

		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.HOUSE_BUY);
		predicates.add(categoryPredicate);

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

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Furniture"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFurniture())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartHouseArea()) && !filterDto.getStartHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartHouseArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndHouseArea()) && !filterDto.getEndHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndHouseArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Renovation"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRenovation())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGarage()) && !filterDto.getGarage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Garage"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getGarage())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Appliances"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAppliances())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFacilities()) && !filterDto.getFacilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Facilities"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFacilities())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Service Lines"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getServiceLines())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartLandArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndLandArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the Building"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFloorsInTheBuilding())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms())
			);
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(HouseRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.HOUSE_RENTAL);
		predicates.add(categoryPredicate);

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

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Furniture"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFurniture())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartHouseArea()) && !filterDto.getStartHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartHouseArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndHouseArea()) && !filterDto.getEndHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndHouseArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Renovation"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRenovation())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGarage()) && !filterDto.getGarage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Garage"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getGarage())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Appliances"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAppliances())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Amenities"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAmenities())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFacilities()) && !filterDto.getFacilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Facilities"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFacilities())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Service Lines"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getServiceLines())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartLandArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndLandArea())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the Building"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFloorsInTheBuilding())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithChildren()) && !filterDto.getWithChildren().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Children"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithChildren())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Pets"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithPets())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilityPayments()) && !filterDto.getUtilityPayments().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Utility Payments"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getUtilityPayments())
			);
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Prepayment"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getPrepayment())
			);
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(AutoFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.CAR);
		predicates.add(categoryPredicate);

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

		if (nonNull(filterDto.getEngineType()) && !filterDto.getEngineType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Engine Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getEngineType())
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