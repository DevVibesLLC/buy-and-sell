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
import am.devvibes.buyandsell.exception.FileIsNullException;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.exception.UnsupportedExtensionException;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.repository.item.ItemRepository;
import am.devvibes.buyandsell.service.favoriteItems.FavoriteItemsService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class  ItemServiceImpl implements ItemService {

	private final ItemRepository itemRepository;
	private final ItemMapper itemMapper;
	private final SecurityService securityService;
	private final ValueService valueService;
	private final EntityManager entityManager;
	private final FavoriteItemsService favoriteItemsService;

	@Override
	@Transactional
	public ItemResponseDto save(ItemRequestDto itemRequestDto, List<MultipartFile> images, Long categoryId) {
		imagesValidation(images);
		ItemEntity itemEntity = itemMapper.mapDtoToEntity(itemRequestDto, images, categoryId);
		return itemMapper.mapEntityToDto(itemRepository.save(itemEntity));
	}

	@Override
	@Transactional
	public ItemResponseDto findById(Long id) {
		ItemEntity itemEntity = getItemByIdOrElseThrow(id);
		if (itemEntity.getStatus().equals(Status.CREATED))
			return itemMapper.mapEntityToDto(itemEntity);
		throw new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND);
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
				Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
						"%" + searchDto.getStroke().toLowerCase() + "%");
				predicates.add(nameLike);

				Predicate lastNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
						"%" + searchDto.getStroke().toLowerCase() + "%");
				predicates.add(lastNameLike);
			}
			return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
		});
		return itemMapper.mapEntityListToDtoList(itemRepository.findAll(specification));
	}

	@Override
	public List<ItemResponseDto> filterItems(AutoFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.CARS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getModel()) && !filterDto.getModel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Model"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getModel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartYear()) && !filterDto.getStartYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Year"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndYear()) && !filterDto.getEndYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Year"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBodyType()) && !filterDto.getBodyType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Body Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getBodyType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Engine Size"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartEngineSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndEngineSize()) && !filterDto.getEndEngineSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Engine Size"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndEngineSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTransmission()) && !filterDto.getTransmission().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Transmission"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getTransmission()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getDriveType()) && !filterDto.getDriveType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Drive Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getDriveType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEngineType()) && !filterDto.getEngineType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Engine Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getEngineType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartMileage()) && !filterDto.getStartMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mileage"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
							filterDto.getStartMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndMileage()) && !filterDto.getEndMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mileage"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSteeringWheel()) && !filterDto.getSteeringWheel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Steering Wheel"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getSteeringWheel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getClearedCustom()) && !filterDto.getClearedCustom().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Cleared Custom"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getClearedCustom()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWheelSize()) && !filterDto.getWheelSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Wheel Size"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWheelSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getHeadlights()) && !filterDto.getHeadlights().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Headlights"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getHeadlights()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getInteriorColor()) && !filterDto.getInteriorColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Interior Color"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getInteriorColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getExteriorColor()) && !filterDto.getExteriorColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Exterior Color"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getExteriorColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSunroof()) && !filterDto.getSunroof().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Sunroof"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getSunroof()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);

	}

	@Override
	public List<ItemResponseDto> filterItems(TruckFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.TRUCKS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getModel()) && !filterDto.getModel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Model"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getModel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getChassiesConfiguration()) && !filterDto.getChassiesConfiguration().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Chassis configuration"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"),
									filterDto.getChassiesConfiguration()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartYear()) && !filterDto.getStartYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Year"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndYear()) && !filterDto.getEndYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Year"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Steering Wheel"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getSteeringWheel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTransmission()) && !filterDto.getTransmission().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Transmission"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getTransmission()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEngineType()) && !filterDto.getEngineType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Engine Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getEngineType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartMileage()) && !filterDto.getStartMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mileage"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
							filterDto.getStartMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndMileage()) && !filterDto.getEndMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mileage"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getClearedCustom()) && !filterDto.getClearedCustom().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Cleared Custom"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getClearedCustom()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getColor()));
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

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.BUSES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getModel()) && !filterDto.getModel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Model"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getModel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartYear()) && !filterDto.getStartYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Year"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndYear()) && !filterDto.getEndYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Year"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Steering Wheel"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getSteeringWheel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTransmission()) && !filterDto.getTransmission().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Transmission"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getTransmission()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEngineType()) && !filterDto.getEngineType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Engine Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getEngineType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartMileage()) && !filterDto.getStartMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mileage"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
							filterDto.getStartMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndMileage()) && !filterDto.getEndMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mileage"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getClearedCustom()) && !filterDto.getClearedCustom().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Cleared Custom"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getClearedCustom()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getColor()));
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

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.APARTMENTS_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"),
			"Elevator"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNewConstruction()) && !filterDto.getNewConstruction().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "New Construction"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNewConstruction()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the " +
					 "Building"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"),
									filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Appliances"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloor()) && !filterDto.getStartFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloor()) && !filterDto.getEndFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBalcony()) && !filterDto.getBalcony().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Balcony"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getBalcony()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCeilingHeight()) && !filterDto.getCeilingHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Ceiling Height"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCeilingHeight()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Furniture"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Renovation"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Parking"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWindowViews()) && !filterDto.getWindowViews().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Windows Views"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWindowViews()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTheHouseHas()) && !filterDto.getTheHouseHas().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "The House Has"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getTheHouseHas()));
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

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.APARTMENTS_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"),
			"Elevator"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNewConstruction()) && !filterDto.getNewConstruction().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "New Construction"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNewConstruction()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the " +
					 "Building"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"),
							 filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Appliances"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Amenities"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloor()) && !filterDto.getStartFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloor()) && !filterDto.getEndFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBalcony()) && !filterDto.getBalcony().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Balcony"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getBalcony()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCeilingHeight()) && !filterDto.getCeilingHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Ceiling Height"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCeilingHeight()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Furniture"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Renovation"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Parking"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWindowViews()) && !filterDto.getWindowViews().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Windows Views"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWindowViews()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTheHouseHas()) && !filterDto.getTheHouseHas().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "The House Has"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getTheHouseHas()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Pets"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithPets()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithChildren()) && !filterDto.getWithChildren().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Children"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithChildren()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Prepayment"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getPrepayment()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilityPayments()) && !filterDto.getUtilityPayments().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Utility Payments"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getUtilityPayments()));
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

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.HOUSES_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Furniture"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartHouseArea()) && !filterDto.getStartHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndHouseArea()) && !filterDto.getEndHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Renovation"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGarage()) && !filterDto.getGarage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Garage"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getGarage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Appliances"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFacilities()) && !filterDto.getFacilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Facilities"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFacilities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Service Lines"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getServiceLines()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the " +
					 "Building"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"),
							 filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms()));
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

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.HOUSES_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Furniture"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartHouseArea()) && !filterDto.getStartHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndHouseArea()) && !filterDto.getEndHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Renovation"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGarage()) && !filterDto.getGarage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Garage"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getGarage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Appliances"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Amenities"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFacilities()) && !filterDto.getFacilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Facilities"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFacilities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Service Lines"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getServiceLines()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the " +
 "Building"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"),
									filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithChildren()) && !filterDto.getWithChildren().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Children"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithChildren()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Pets"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithPets()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilityPayments()) && !filterDto.getUtilityPayments().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Utility Payments"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getUtilityPayments()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Prepayment"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getPrepayment()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(CommercialBuyFilterDto filterDto) {

		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.COMMERCIALS_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Parking"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Furniture"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getElevator()) && !filterDto.getElevator().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"),
			"Elevator"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEntrance()) && !filterDto.getEntrance().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"),
					"Entrance"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getEntrance()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getLocationFromTheStreet()) && !filterDto.getLocationFromTheStreet().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Location from the Street"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getLocationFromTheStreet()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);

	}

	@Override
	public List<ItemResponseDto> filterItems(CommercialRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.COMMERCIALS_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Parking"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Furniture"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getElevator()) && !filterDto.getElevator().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"),
			"Elevator"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEntrance()) && !filterDto.getEntrance().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"),
							"Entrance"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getEntrance()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getLocationFromTheStreet()) && !filterDto.getLocationFromTheStreet().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Location from the Street"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getLocationFromTheStreet()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilityPayments()) && !filterDto.getUtilityPayments().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Utility Payments"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getUtilityPayments()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Prepayment"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getPrepayment()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMinimumRentalPeriod()) && !filterDto.getMinimumRentalPeriod().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Minimum Rental Period"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"),
							 filterDto.getMinimumRentalPeriod()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getLeaseType()) && !filterDto.getLeaseType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Lease Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getLeaseType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(GarageAndParkingBuyFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.GARAGES_AND_PARKING_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Amenities"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilities()) && !filterDto.getUtilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Utilities"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getUtilities()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(GarageAndParkingRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.GARAGES_AND_PARKING_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Amenities"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilities()) && !filterDto.getUtilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Utilities"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getUtilities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Prepayment"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getPrepayment()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(LandBuyFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.LANDS_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Service Lines"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getServiceLines()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(LandRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.LANDS_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Service Lines"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getServiceLines()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Prepayment"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getPrepayment()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(NewConstructionApartmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.NEW_CONSTRUCTION_APARTMENTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"),
			"Elevator"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the " +
					 "Building"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"),
							 filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloor()) && !filterDto.getStartFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloor()) && !filterDto.getEndFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTheHouseHas()) && !filterDto.getTheHouseHas().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "The House Has"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getTheHouseHas()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBalcony()) && !filterDto.getBalcony().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Balcony"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getBalcony()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCeilingHeight()) && !filterDto.getCeilingHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Ceiling Height"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCeilingHeight()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Parking"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getInteriorFinishing()) && !filterDto.getInteriorFinishing().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Interior Finishing"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getInteriorFinishing()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMortgageIsPossible()) && !filterDto.getMortgageIsPossible().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mortgage is Possible"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMortgageIsPossible()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(NewConstructionHouseFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.NEW_CONSTRUCTION_APARTMENTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the " +
					 "Building"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"),
							 filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartHouseArea()) && !filterDto.getStartHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndHouseArea()) && !filterDto.getEndHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Land Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGarage()) && !filterDto.getGarage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Garage"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getGarage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Service Lines"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getServiceLines()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getInteriorFinishing()) && !filterDto.getInteriorFinishing().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Interior Finishing"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getInteriorFinishing()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMortgageIsPossible()) && !filterDto.getMortgageIsPossible().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mortgage is Possible"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMortgageIsPossible()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(ApartmentDailyRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.APARTMENTS_DAILY_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNewConstruction()) && !filterDto.getNewConstruction().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "New Construction"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNewConstruction()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTheHouseHas()) && !filterDto.getTheHouseHas().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "The House Has"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getTheHouseHas()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the " +
					 "Building"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"),
							 filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloor()) && !filterDto.getStartFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloor()) && !filterDto.getEndFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Parking"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Amenities"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Appliances"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWindowViews()) && !filterDto.getWindowViews().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Window Views"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWindowViews()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Pets"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithPets()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithChildren()) && !filterDto.getWithChildren().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Children"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithChildren()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfGuests()) && !filterDto.getNumberOfGuests().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Guests"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfGuests()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Renovation"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBalcony()) && !filterDto.getBalcony().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Balcony"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getBalcony()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getElevator()) && !filterDto.getElevator().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"),
			"Elevator"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCeilingHeight()) && !filterDto.getCeilingHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Ceiling Height"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCeilingHeight()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getComfort()) && !filterDto.getComfort().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Comfort"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getComfort()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(HouseDailyRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.HOUSES_DAILY_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Construction Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floors in the " +
									"Building"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"),
							 filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartHouseArea()) && !filterDto.getStartHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndHouseArea()) && !filterDto.getEndHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "House Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGarage()) && !filterDto.getGarage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Garage"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getGarage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Amenities"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Appliances"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Pets"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithPets()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithChildren()) && !filterDto.getWithChildren().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Children"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithChildren()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfGuests()) && !filterDto.getNumberOfGuests().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Guests"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfGuests()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Renovation"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Rooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Bathrooms"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getComfort()) && !filterDto.getComfort().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Comfort"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getComfort()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(MobilePhoneFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.MOBILE_PHONES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getModel()) && !filterDto.getModel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Model"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getModel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStorage()) && !filterDto.getStorage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Storage"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getStorage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getColor()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(NotebookFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.NOTEBOOKS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMemory()) && !filterDto.getMemory().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Memory"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMemory()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMemoryRAM()) && !filterDto.getMemoryRAM().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Memory (RAM)"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMemoryRAM()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getProcessor()) && !filterDto.getProcessor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Processor"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getProcessor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenSize()) && !filterDto.getScreenSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Screen Size"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getScreenSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenResolution()) && !filterDto.getScreenResolution().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Screen Resolution"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getScreenResolution()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(ComputerFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.COMPUTERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMemory()) && !filterDto.getMemory().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Memory"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMemory()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMemoryRAM()) && !filterDto.getMemoryRAM().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Memory (RAM)"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMemoryRAM()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getProcessor()) && !filterDto.getProcessor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Processor"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getProcessor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenSize()) && !filterDto.getScreenSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Screen Size"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getScreenSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenResolution()) && !filterDto.getScreenResolution().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Screen Resolution"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getScreenResolution()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(SmartWatchFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.SMART_WATCHES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getColor()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(TabletFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.TABLETS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenSize()) && !filterDto.getScreenSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Screen Size"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getScreenSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMemory()) && !filterDto.getMemory().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Memory"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMemory()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getColor()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(TVFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.TV_STREAMERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenSize()) && !filterDto.getScreenSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Screen Size"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getScreenSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(GamingConsoleFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.GAMING_CONSOLES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(HeadphoneFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.HEADPHONES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConnectionType()) && !filterDto.getConnectionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Connection Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getConnectionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(ComputerAndNotebookPartsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.COMPUTER_AND_NOTEBOOK_PARTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(PhotoAndVideoCameraFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.PHOTO_AND_VIDEO_CAMERAS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(ComputerGamesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.COMPUTER_GAMES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(SmartHomeAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.SMART_HOME_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(WasherFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate = criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.WASHERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMaximumLaundryCapacity()) && !filterDto.getMaximumLaundryCapacity().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Maximum Laundry Capacity"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMaximumLaundryCapacity()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getLaundryLoadType()) && !filterDto.getLaundryLoadType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Laundry Load Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getLaundryLoadType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(ClothesDryerFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.CLOTHES_DRYERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(IronAndAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.IRONS_AND_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
					 filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
			 filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(RefrigeratorFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.REFRIGERATORS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(FreezerFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.FREEZERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(DishwasherFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.DISHWASHERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(MicrowaveFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.MICROWAVES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(StoveFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.STOVES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBurnerType()) && !filterDto.getBurnerType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Burner Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getBurnerType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(CoffeeMakerAndAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.COFFEE_MAKERS_AND_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(KettleFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.KETTLES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(RangeHoodFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.RANGE_HOODS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(VacuumCleanerFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.VACUUM_CLEANERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(RoboticVacuumFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.ROBOTIC_VACUUMS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(FloorWasherFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.FLOOR_WASHERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(AirConditionerFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.AIR_CONDITIONERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(WaterHeatersFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.WATER_HEATERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(AirPurifiersAndHumidifiersFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.AIR_PURIFIERS_AND_HUMIDIFIERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(ComputerPeripheralFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.COMPUTERS_PERIPHERALS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(AudioPlayerAndStereoFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.AUDIO_PLAYERS_AND_STEREOS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(QuadcoptersAndDronesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.QUADCOPTERS_AND_DRONES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(SofaAndArmchairFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.QUADCOPTERS_AND_DRONES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUpholstery()) && !filterDto.getUpholstery().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Upholstery"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getUpholstery()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Color"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(StorageFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.STORAGE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(TableAndChairFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.TABLES_AND_CHAIRS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(BedroomFurnitureFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.BEDROOM_FURNITURE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(KitchenFurnitureFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.KITCHEN_FURNITURE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Color"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(GardenFurnitureFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.GARDEN_FURNITURE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(BarbecueAndAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.BARBECUE_AND_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(GardenDecorFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.GARDEN_DECOR);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(GardenAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.GARDEN_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(LightingFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.LIGHTING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(TextileFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.TEXTILES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(RugFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.RUGS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRugWidth()) && !filterDto.getRugWidth().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Rug Width"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRugWidth()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRugLength()) && !filterDto.getRugLength().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Rug Length"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getRugLength()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(InteriorDecorationFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.INTERIOR_DECORATION);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(TablewareFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.TABLEWARE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(CookingAndBakingFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.COOKING_AND_BAKING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(KitchenAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.KITCHEN_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(BathroomAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.BATHROOM_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(VideoSurveillanceFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.VIDEO_SURVEILLANCE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(CarPartFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.CAR_PARTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Mark"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getOriginality()) && !filterDto.getOriginality().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Originality"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getOriginality()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPartSide()) && !filterDto.getPartSide().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Part Side"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getPartSide()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPartPosition()) && !filterDto.getPartPosition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Part Position"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getPartPosition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(WheelAndTireFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.WHEELS_AND_TIRES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSeason()) && !filterDto.getSeason().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Season"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getSeason()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWidth()) && !filterDto.getWidth().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Width"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWidth()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getHeight()) && !filterDto.getHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Height"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getHeight()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getDiameter()) && !filterDto.getDiameter().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Diameter"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getDiameter()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(RimAndHubCapFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.RIMS_AND_HUB_CAPS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getDiameter()) && !filterDto.getDiameter().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Diameter"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getDiameter()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(CarBatteryFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.CAR_BATTERIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getVoltage()) && !filterDto.getVoltage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Voltage"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getVoltage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartCapacity()) && !filterDto.getStartCapacity().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Capacity"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getStartCapacity()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndCapacity()) && !filterDto.getEndCapacity().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Capacity"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"), filterDto.getEndCapacity()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(GasEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.GAS_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(OilAndChemicalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.OILS_AND_CHEMICALS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(CarAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.CAR_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(CarElectronicFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.CAR_ELECTRONICS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(CarAudioAndVideoFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.CAR_AUDIO_AND_VIDEO);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(PersonalTransportationFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.PERSONAL_TRANSPORTATION);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(AtvAndSnowmobileFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.ATVS_AND_SNOWMOBILES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(BoatAndWaterTransportFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.BOATS_AND_WATER_TRANSPORT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Condition"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(TrailerAndBoothFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.TRAILERS_AND_BOOTHS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getExteriorFinish()) && !filterDto.getExteriorFinish().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Exterior Finish"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getExteriorFinish()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		List<ItemEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
		return itemMapper.mapEntityListToDtoList(resultList);
	}

	@Override
	public List<ItemResponseDto> filterItems(EventVenueRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get("category").get("name"), CategoryEnum.EVENT_VENUES_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get("price").get("price"),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get("price").get("price"), filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate = criteriaBuilder.equal(itemRoot.get("price").get("currency"),
					filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get("location").get("country"), filterDto.getCountry());
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
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Type"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Floor Area"),
							criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue"),
									filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfGuests()) && !filterDto.getNumberOfGuests().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Number of Guests"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNumberOfGuests()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEventTypes()) && !filterDto.getEventTypes().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Event Types"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getEventTypes()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEquipment()) && !filterDto.getEquipment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Equipment"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getEquipment()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFacilities()) && !filterDto.getFacilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Facilities"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getFacilities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNoiseAfterHours()) && !filterDto.getNoiseAfterHours().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "Noise After Hours"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getNoiseAfterHours()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join("fields", JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin = itemFieldJoin.join("fieldName", JoinType.INNER);
			Predicate predicate =
					criteriaBuilder.and(criteriaBuilder.equal(fieldNameJoin.get("fieldName"), "With Pets"),
							criteriaBuilder.equal(itemFieldJoin.get("fieldValue"), filterDto.getWithPets()));
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

	private void imagesValidation(List<MultipartFile> images) {
		if(images.size() >= 15)
			throw new SomethingWentWrongException(ExceptionConstants.COUNT_OF_IMAGES_MUST_BE_LESS_THEN_15);
		areAllFilesWithAllowedExtensions(images);
	}

	private void areAllFilesWithAllowedExtensions(List<MultipartFile> images) {
		List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "svg");

		for (MultipartFile image : images) {
			String fileName = image.getOriginalFilename();
			if (!isNull(fileName)) {
				String extension = getFileExtension(fileName);
				if (!allowedExtensions.contains(extension.toLowerCase())) {
					throw new UnsupportedExtensionException(ExceptionConstants.UNSUPPORTED_FILE_EXTENSION);
				}
			} else {
				throw new FileIsNullException(ExceptionConstants.FILE_NAME_IS_NULL);
			}
		}
	}

	private String getFileExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
			return fileName.substring(dotIndex + 1);
		} else {
			return "";
		}
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
				.country(LocationEnum.getCountry(
						isNull(itemRequestDto.getCityId()) ? itemEntity.getLocation().getCity().getId() :
								itemRequestDto.getCityId()))
				.city(LocationEnum.getCity(
						isNull(itemRequestDto.getCityId()) ? itemEntity.getLocation().getCity().getId() :
								itemRequestDto.getCityId()))
				.region(LocationEnum.getRegion(
						isNull(itemRequestDto.getCityId()) ? itemEntity.getLocation().getCity().getId() :
								itemRequestDto.getCityId()))
				.address(isNull(itemRequestDto.getAddress()) ? itemEntity.getLocation().getAddress() :
						itemRequestDto.getAddress())
				.build());
		itemEntity.setUpdatedAt(ZonedDateTime.now());
		return itemRepository.save(itemEntity);
	}

	private ItemEntity getItemByIdOrElseThrow(Long id) {
		return itemRepository.findById(id).orElseThrow(() -> new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND));
	}

}