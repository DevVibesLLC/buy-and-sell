package am.devvibes.buyandsell.service.item.impl;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.filter.*;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.item.ItemUpdateDto;
import am.devvibes.buyandsell.dto.priceStatistic.PriceStatisticsRequestDto;
import am.devvibes.buyandsell.dto.search.SearchDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;
import am.devvibes.buyandsell.entity.field.FieldEntity;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.priceHistory.PriceHistoryEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.repository.item.ItemRepository;
import am.devvibes.buyandsell.repository.priceHistory.PriceHistoryRepository;
import am.devvibes.buyandsell.repository.user.UserRepository;
import am.devvibes.buyandsell.service.favoriteItems.FavoriteItemsService;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.service.location.LocationService;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.value.ValueService;
import am.devvibes.buyandsell.util.CategoryEnum;
import am.devvibes.buyandsell.util.ExceptionConstants;
import am.devvibes.buyandsell.util.FilterConstants;
import am.devvibes.buyandsell.util.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
	private final PriceHistoryRepository priceHistoryRepository;
	private final UserRepository userRepository;
	private final FavoriteItemsService favoriteItemsService;
	private final LocationService locationService;

	@Override
	@Transactional
	public ItemEntity save(ItemRequestDto itemRequestDto, Long categoryId) {
		ItemEntity itemEntity = itemMapper.mapDtoToEntity(itemRequestDto, categoryId);
		ItemEntity savedEntity = itemRepository.save(itemEntity);
		return initialPriceHistoryForNewItem(savedEntity);
	}

	@Override
	@Transactional
	public ItemEntity saveFromBusiness(ItemRequestDto itemRequestDto, BusinessPageEntity businessPageEntity) {
		ItemEntity itemEntity = itemMapper.mapDtoToEntityFromBusiness(itemRequestDto, businessPageEntity);
		ItemEntity savedEntity = itemRepository.save(itemEntity);
		return initialPriceHistoryForNewItem(savedEntity);
	}

	@Override
	@Transactional
	public ItemEntity findById(Long id) {
		ItemEntity itemEntity = getItemByIdOrElseThrow(id);

		if (itemEntity.getStatus().equals(Status.CREATED)) {
			if (!itemEntity.getViewedUsersId().contains(securityService.getCurrentUserId())) {
				itemEntity.getViewedUsersId().add(securityService.getCurrentUserId());
			}
			return incrementCountOfViewsAndReturnItem(itemEntity);
		}
		throw new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND);
	}

	private ItemEntity incrementCountOfViewsAndReturnItem(ItemEntity itemEntity) {
		itemEntity.setCountOfViews(itemEntity.getCountOfViews() + 1);
		return itemRepository.save(itemEntity);
	}

	@Override
	public ItemEntity findEntityById(Long id) {
		return itemRepository.findById(id).orElseThrow(() -> new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND));
	}

	@Override
	public List<ItemEntity> findUsersItems() {
		return itemRepository.findByUserEntity(userRepository.findById(securityService.getCurrentUserId())
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.USER_NOT_FOUND)));
	}

	@Override
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
	public ItemEntity update(ItemUpdateDto itemUpdateDto, Long categoryId, Long itemId) {
		ItemEntity itemEntity = getItemByIdOrElseThrow(itemId);
		if (itemEntity.getStatus().equals(Status.DELETED)) {
			throw new SomethingWentWrongException(ExceptionConstants.INVALID_ACTION);
		}

		BigDecimal oldPrice = itemEntity.getPrice().getPrice();

		ItemEntity updatedEntity = updateEntity(itemEntity, itemUpdateDto);
		if (!oldPrice.equals(updatedEntity.getPrice().getPrice())) {
			sendMessageAboutPriceChanging(updatedEntity.getId());
			return saveOrChangePriceHistory(updatedEntity);
		}
		return updatedEntity;
	}

	@Override
	@Transactional
	public ItemEntity updateFromBusiness(ItemUpdateDto itemUpdateDto,
			BusinessPageEntity businessPageEntity,
			Long itemId) {
		ItemEntity itemEntity = getItemByIdOrElseThrow(itemId);
		if (!itemEntity.getBusinessPage().getId().equals(businessPageEntity.getId()) ||
				itemEntity.getStatus().equals(Status.DELETED)) {
			throw new SomethingWentWrongException(ExceptionConstants.INVALID_ACTION);
		}
		BigDecimal oldPrice = itemEntity.getPrice().getPrice();

		ItemEntity updatedEntity = updateEntity(itemEntity, itemUpdateDto);
		if (!oldPrice.equals(updatedEntity.getPrice().getPrice())) {
			sendMessageAboutPriceChanging(updatedEntity.getId());
			return saveOrChangePriceHistory(updatedEntity);
		}
		return updatedEntity;
	}

	private void sendMessageAboutPriceChanging(Long itemId) {
		List<String> usersIdsByItemId = favoriteItemsService.getUsersIdsByItemId(itemId);
		List<UserEntity> userEntities = userRepository.findAllById(usersIdsByItemId);
		//TODO, write method for sending emails.
	}

	@Override
	public List<ItemEntity> searchItems(SearchDto searchDto) {
		Specification<ItemEntity> specification = Specification.where((root, criteriaQuery, criteriaBuilder) -> {
			var predicates = new ArrayList<Predicate>();

			if (nonNull(searchDto.getStroke())) {
				Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get(FilterConstants.title)),
						"%" + searchDto.getStroke().toLowerCase() + "%");
				predicates.add(nameLike);

				Predicate lastNameLike =
						criteriaBuilder.like(criteriaBuilder.lower(root.get(FilterConstants.description)),
								"%" + searchDto.getStroke().toLowerCase() + "%");
				predicates.add(lastNameLike);
			}
			return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
		});
		return itemRepository.findAll(specification);
	}

	private void addCategoryPredicate(CategoryEnum category,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name), category);
		predicates.add(categoryPredicate);
	}

	private void addMarkPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getMark_eng()) && !filterDto.getMark_eng().isEmpty()) || (nonNull(filterDto.getMark_ru()) && !filterDto.getMark_ru().isEmpty()) || (nonNull(filterDto.getMark_hy()) && !filterDto.getMark_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Mark"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getMark_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Марка"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getMark_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Մակնիշ"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getMark_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addModelPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getModel_eng()) && !filterDto.getModel_eng().isEmpty()) || (nonNull(filterDto.getModel_ru()) && !filterDto.getModel_ru().isEmpty()) || (nonNull(filterDto.getModel_hy()) && !filterDto.getModel_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Model"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getModel_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Модель"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getModel_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Մոդել"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getModel_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addStartYearPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getStartYear_eng()) && !filterDto.getStartYear_eng().isEmpty()) || (nonNull(filterDto.getStartYear_ru()) && !filterDto.getStartYear_ru().isEmpty()) || (nonNull(filterDto.getStartYear_hy()) && !filterDto.getStartYear_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Year"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getStartYear_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Год"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getStartYear_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Տարեթիվ"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getStartYear_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addEndYearPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getEndYear_eng()) && !filterDto.getEndYear_eng().isEmpty()) || (nonNull(filterDto.getEndYear_ru()) && !filterDto.getEndYear_ru().isEmpty()) || (nonNull(filterDto.getEndYear_hy()) && !filterDto.getEndYear_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Year"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getEndYear_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Год"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getEndYear_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Տարեթիվ"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getEndYear_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addBodyTypePredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getBodyType_eng()) && !filterDto.getBodyType_eng().isEmpty()) || (nonNull(filterDto.getBodyType_ru()) && !filterDto.getBodyType_ru().isEmpty()) || (nonNull(filterDto.getBodyType_hy()) && !filterDto.getBodyType_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Body Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getBodyType_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Тип Кузова"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getBodyType_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Թափքի Տեսակ"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getBodyType_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addStartEngineSizePredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getStartEngineSize_eng()) && !filterDto.getStartEngineSize_eng().isEmpty()) || (nonNull(filterDto.getStartEngineSize_ru()) && !filterDto.getStartEngineSize_ru().isEmpty()) || (nonNull(filterDto.getStartEngineSize_hy()) && !filterDto.getStartEngineSize_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Engine Size"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getStartEngineSize_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Объём Двигателя"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getStartEngineSize_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Շարժիչի Ծավալ"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getStartEngineSize_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addEndEngineSizePredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getEndEngineSize_eng()) && !filterDto.getEndEngineSize_eng().isEmpty()) || (nonNull(filterDto.getEndEngineSize_ru()) && !filterDto.getEndEngineSize_ru().isEmpty()) || (nonNull(filterDto.getEndEngineSize_hy()) && !filterDto.getEndEngineSize_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Engine Size"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getEndEngineSize_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Объём Двигателя"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getEndEngineSize_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Շարժիչի Ծավալ"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getEndEngineSize_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addTransmissionPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getTransmission_eng()) && !filterDto.getTransmission_eng().isEmpty()) || (nonNull(filterDto.getTransmission_ru()) && !filterDto.getTransmission_ru().isEmpty()) || (nonNull(filterDto.getTransmission_hy()) && !filterDto.getTransmission_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Transmission"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getTransmission_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Коробка Передач"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getTransmission_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Փոխանցման Տուփ"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getTransmission_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addDriveTypePredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getDriveType_eng()) && !filterDto.getDriveType_eng().isEmpty()) || (nonNull(filterDto.getDriveType_ru()) && !filterDto.getDriveType_ru().isEmpty()) || (nonNull(filterDto.getDriveType_hy()) && !filterDto.getDriveType_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Drive Type"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getDriveType_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Привод"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getDriveType_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Քարշակ"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getDriveType_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addEngineTypePredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getEngineType_eng()) && !filterDto.getEngineType_eng().isEmpty()) || (nonNull(filterDto.getEngineType_ru()) && !filterDto.getEngineType_ru().isEmpty()) || (nonNull(filterDto.getEngineType_hy()) && !filterDto.getEngineType_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Engine Type"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getEngineType_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Тип Двигателя"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getEngineType_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Շարժիչի Տեսակ"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getEngineType_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addStartMileagePredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getStartMileage_eng()) && !filterDto.getStartMileage_eng().isEmpty()) || (nonNull(filterDto.getStartMileage_ru()) && !filterDto.getStartMileage_ru().isEmpty()) || (nonNull(filterDto.getStartMileage_hy()) && !filterDto.getStartMileage_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Mileage"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getStartMileage_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Пробег"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getStartMileage_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Վազք"),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getStartMileage_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addEndMileagePredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getEndMileage_eng()) && !filterDto.getEndMileage_eng().isEmpty()) || (nonNull(filterDto.getEndMileage_ru()) && !filterDto.getEndMileage_ru().isEmpty()) || (nonNull(filterDto.getEndMileage_hy()) && !filterDto.getEndMileage_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Mileage"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getEndMileage_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Пробег"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getEndMileage_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Վազք"),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getEndMileage_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addSteeringWheelPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getSteeringWheel_eng()) && !filterDto.getSteeringWheel_eng().isEmpty()) || (nonNull(filterDto.getSteeringWheel_ru()) && !filterDto.getSteeringWheel_ru().isEmpty()) || (nonNull(filterDto.getSteeringWheel_hy()) && !filterDto.getSteeringWheel_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Steering Wheel"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getSteeringWheel_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Руль"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getSteeringWheel_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Ղեկ"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getSteeringWheel_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addClearedCustomPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getClearedCustom_eng()) && !filterDto.getClearedCustom_eng().isEmpty()) || (nonNull(filterDto.getClearedCustom_ru()) && !filterDto.getClearedCustom_ru().isEmpty()) || (nonNull(filterDto.getClearedCustom_hy()) && !filterDto.getClearedCustom_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Cleared Custom"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getClearedCustom_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Растаможка"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getClearedCustom_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Մաքսազերծում"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getClearedCustom_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addColorPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getColor_eng()) && !filterDto.getColor_eng().isEmpty()) || (nonNull(filterDto.getColor_ru()) && !filterDto.getColor_ru().isEmpty()) || (nonNull(filterDto.getColor_hy()) && !filterDto.getColor_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getColor_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Цвет"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getColor_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Գույն"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getColor_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addWheelSizePredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getWheelSize_eng()) && !filterDto.getWheelSize_eng().isEmpty()) || (nonNull(filterDto.getWheelSize_ru()) && !filterDto.getWheelSize_ru().isEmpty()) || (nonNull(filterDto.getWheelSize_hy()) && !filterDto.getWheelSize_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Wheel Size"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getWheelSize_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Размер Колес"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getWheelSize_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Անիվի Չափս"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getWheelSize_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addHeadlightsPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getHeadlights_eng()) && !filterDto.getHeadlights_eng().isEmpty()) || (nonNull(filterDto.getHeadlights_ru()) && !filterDto.getHeadlights_ru().isEmpty()) || (nonNull(filterDto.getHeadlights_hy()) && !filterDto.getHeadlights_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Headlights"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getHeadlights_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Фары"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getHeadlights_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Լուսարձակներ"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getHeadlights_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addInteriorColorPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getInteriorColor_eng()) && !filterDto.getInteriorColor_eng().isEmpty()) || (nonNull(filterDto.getInteriorColor_ru()) && !filterDto.getInteriorColor_ru().isEmpty()) || (nonNull(filterDto.getInteriorColor_hy()) && !filterDto.getInteriorColor_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Interior Color"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getInteriorColor_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Цвет Салона"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getInteriorColor_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Սրահի Գույն"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getInteriorColor_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addInteriorMaterialPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getInteriorMaterial_eng()) && !filterDto.getInteriorMaterial_eng().isEmpty()) || (nonNull(filterDto.getInteriorMaterial_ru()) && !filterDto.getInteriorMaterial_ru().isEmpty()) || (nonNull(filterDto.getInteriorMaterial_hy()) && !filterDto.getInteriorMaterial_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Interior Material"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getInteriorMaterial_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Материал Салона"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getInteriorMaterial_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Սրահ"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getInteriorMaterial_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addSunroofPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if ((nonNull(filterDto.getSunroof_eng()) && !filterDto.getSunroof_eng().isEmpty()) || (nonNull(filterDto.getSunroof_ru()) && !filterDto.getSunroof_ru().isEmpty()) || (nonNull(filterDto.getSunroof_hy()) && !filterDto.getSunroof_hy().isEmpty())) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);

			Predicate predicate_eng = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_eng"), "Sunroof"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_eng"),
							filterDto.getSunroof_eng()));

			Predicate predicate_ru = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_ru"), "Люк"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_ru"),
							filterDto.getSunroof_ru()));

			Predicate predicate_hy = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get("fieldName_hy"), "Լյուկ"),
					criteriaBuilder.equal(itemFieldJoin.get("fieldValue_hy"),
							filterDto.getSunroof_hy()));

			Predicate combinedPredicate = criteriaBuilder.or(predicate_eng, predicate_ru, predicate_hy);

			predicates.add(combinedPredicate);
		}
	}

	private void addStartPricePredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}
	}

	private void addEndPricePredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}
	}

	private void addCurrencyPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}
	}

	private void addCountryPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}
	}

	private void addRegionPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}
	}

	private void addCityPredicate(AutoFilterDto filterDto,
			CriteriaBuilder criteriaBuilder,
			Root<ItemEntity> itemRoot,
			List<Predicate> predicates) {
		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}
	}

	@Override
	public List<ItemEntity> filterItems(AutoFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		addCategoryPredicate(CategoryEnum.CARS, criteriaBuilder, itemRoot, predicates);
		addStartPricePredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addEndPricePredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addCurrencyPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addCountryPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addRegionPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addCityPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addMarkPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addModelPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addStartYearPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addEndYearPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addBodyTypePredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addStartEngineSizePredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addEndEngineSizePredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addTransmissionPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addDriveTypePredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addEngineTypePredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addStartMileagePredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addEndMileagePredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addSteeringWheelPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addClearedCustomPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addColorPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addWheelSizePredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addHeadlightsPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addInteriorColorPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addInteriorMaterialPredicate(filterDto, criteriaBuilder, itemRoot, predicates);
		addSunroofPredicate(filterDto, criteriaBuilder, itemRoot, predicates);

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(TruckFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TRUCKS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getModel()) && !filterDto.getModel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.model),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getModel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getChassiesConfiguration()) && !filterDto.getChassiesConfiguration().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.chassisConfiguration),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getChassiesConfiguration()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartYear()) && !filterDto.getStartYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.year),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndYear()) && !filterDto.getEndYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.year),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSteeringWheel()) && !filterDto.getSteeringWheel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.steeringWheel),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getSteeringWheel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTransmission()) && !filterDto.getTransmission().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.transmission),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getTransmission()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEngineType()) && !filterDto.getEngineType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.engineType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getEngineType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartMileage()) && !filterDto.getStartMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mileage),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndMileage()) && !filterDto.getEndMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mileage),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getClearedCustom()) && !filterDto.getClearedCustom().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.clearedCustom),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getClearedCustom()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(BusFilterDto filterDto) {

		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BUSES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getModel()) && !filterDto.getModel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.model),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getModel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartYear()) && !filterDto.getStartYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.year),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndYear()) && !filterDto.getEndYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.year),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSteeringWheel()) && !filterDto.getSteeringWheel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.steeringWheel),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getSteeringWheel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTransmission()) && !filterDto.getTransmission().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.transmission),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getTransmission()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEngineType()) && !filterDto.getEngineType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.engineType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getEngineType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartMileage()) && !filterDto.getStartMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mileage),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndMileage()) && !filterDto.getEndMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mileage),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getClearedCustom()) && !filterDto.getClearedCustom().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.clearedCustom),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getClearedCustom()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(ApartmentBuyFilterDto filterDto) {

		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.APARTMENTS_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getElevator()) && !filterDto.getElevator().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.elevator),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.constructionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNewConstruction()) && !filterDto.getNewConstruction().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.newConstruction),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNewConstruction()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.floorsInTheBuilding),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.appliances),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloor()) && !filterDto.getStartFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floor),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloor()) && !filterDto.getEndFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floor),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBalcony()) && !filterDto.getBalcony().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.balcony),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getBalcony()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCeilingHeight()) && !filterDto.getCeilingHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.ceilingHeight),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getCeilingHeight()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.furniture),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.renovation),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.parking),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWindowViews()) && !filterDto.getWindowViews().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.windowsViews),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWindowViews()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.numberOfRooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.numberOfBathrooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTheHouseHas()) && !filterDto.getTheHouseHas().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.theHouseHas),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getTheHouseHas()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(ApartmentRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.APARTMENTS_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getElevator()) && !filterDto.getElevator().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.elevator),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.constructionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNewConstruction()) && !filterDto.getNewConstruction().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.newConstruction),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNewConstruction()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.floorsInTheBuilding),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.appliances),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.amenities),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloor()) && !filterDto.getStartFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floor),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloor()) && !filterDto.getEndFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floor),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBalcony()) && !filterDto.getBalcony().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.balcony),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getBalcony()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCeilingHeight()) && !filterDto.getCeilingHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.ceilingHeight),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getCeilingHeight()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.furniture),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.renovation),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.parking),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWindowViews()) && !filterDto.getWindowViews().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.windowsViews),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWindowViews()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.numberOfRooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.numberOfBathrooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTheHouseHas()) && !filterDto.getTheHouseHas().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.theHouseHas),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getTheHouseHas()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.withPets),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWithPets()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithChildren()) && !filterDto.getWithChildren().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.withChildren),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWithChildren()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.prepayment),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getPrepayment()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilityPayments()) && !filterDto.getUtilityPayments().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.utilityPayments),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getUtilityPayments()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(HouseBuyFilterDto filterDto) {

		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.HOUSES_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.constructionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.furniture),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartHouseArea()) && !filterDto.getStartHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.houseArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndHouseArea()) && !filterDto.getEndHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.houseArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.renovation),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGarage()) && !filterDto.getGarage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.garage),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGarage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.appliances),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFacilities()) && !filterDto.getFacilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.facilities),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getFacilities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.serviceLines),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getServiceLines()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.landArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.landArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.floorsInTheBuilding),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.numberOfRooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.numberOfBathrooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(HouseRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.HOUSES_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.constructionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.furniture),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartHouseArea()) && !filterDto.getStartHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.houseArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndHouseArea()) && !filterDto.getEndHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.houseArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.renovation),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGarage()) && !filterDto.getGarage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.garage),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGarage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.appliances),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.amenities),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFacilities()) && !filterDto.getFacilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.facilities),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getFacilities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.serviceLines),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getServiceLines()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.landArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.landArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.floorsInTheBuilding),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.numberOfRooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.numberOfBathrooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithChildren()) && !filterDto.getWithChildren().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.withChildren),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWithChildren()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.withPets),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWithPets()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilityPayments()) && !filterDto.getUtilityPayments().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.utilityPayments),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getUtilityPayments()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.prepayment),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getPrepayment()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(CommercialBuyFilterDto filterDto) {

		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.COMMERCIALS_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.constructionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.parking),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.furniture),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getElevator()) && !filterDto.getElevator().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.elevator),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEntrance()) && !filterDto.getEntrance().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.entrance),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getEntrance()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getLocationFromTheStreet()) && !filterDto.getLocationFromTheStreet().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.locationFromTheStreet),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getLocationFromTheStreet()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(CommercialRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.COMMERCIALS_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.parking),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFurniture()) && !filterDto.getFurniture().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.furniture),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getFurniture()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getElevator()) && !filterDto.getElevator().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.elevator),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEntrance()) && !filterDto.getEntrance().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.entrance),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getEntrance()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getLocationFromTheStreet()) && !filterDto.getLocationFromTheStreet().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.locationFromTheStreet),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getLocationFromTheStreet()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilityPayments()) && !filterDto.getUtilityPayments().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.utilityPayments),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getUtilityPayments()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.prepayment),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getPrepayment()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMinimumRentalPeriod()) && !filterDto.getMinimumRentalPeriod().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.maximumRentalPeriod),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getMinimumRentalPeriod()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getLeaseType()) && !filterDto.getLeaseType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.leaseType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getLeaseType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(GarageAndParkingBuyFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GARAGES_AND_PARKING_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.amenities),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilities()) && !filterDto.getUtilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.utilities),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getUtilities()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(GarageAndParkingRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GARAGES_AND_PARKING_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.amenities),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUtilities()) && !filterDto.getUtilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.utilities),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getUtilities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.prepayment),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getPrepayment()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(LandBuyFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.LANDS_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.landArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.landArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.serviceLines),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getServiceLines()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(LandRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.LANDS_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.landArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.landArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.serviceLines),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getServiceLines()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPrepayment()) && !filterDto.getPrepayment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.prepayment),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getPrepayment()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(NewConstructionApartmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.NEW_CONSTRUCTION_APARTMENTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getElevator()) && !filterDto.getElevator().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.elevator),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.constructionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.floorsInTheBuilding),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloor()) && !filterDto.getStartFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floor),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloor()) && !filterDto.getEndFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floor),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTheHouseHas()) && !filterDto.getTheHouseHas().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.theHouseHas),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getTheHouseHas()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBalcony()) && !filterDto.getBalcony().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.balcony),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getBalcony()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCeilingHeight()) && !filterDto.getCeilingHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.ceilingHeight),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getCeilingHeight()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.parking),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.numberOfRooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.numberOfBathrooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getInteriorFinishing()) && !filterDto.getInteriorFinishing().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.interiorFinishing),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getInteriorFinishing()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMortgageIsPossible()) && !filterDto.getMortgageIsPossible().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.mortgageIsPossible),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getMortgageIsPossible()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(NewConstructionHouseFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.NEW_CONSTRUCTION_APARTMENTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.constructionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.floorsInTheBuilding),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartHouseArea()) && !filterDto.getStartHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.houseArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndHouseArea()) && !filterDto.getEndHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.houseArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartLandArea()) && !filterDto.getStartLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.landArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndLandArea()) && !filterDto.getEndLandArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.landArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndLandArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGarage()) && !filterDto.getGarage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.garage),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGarage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.numberOfRooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.numberOfBathrooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getServiceLines()) && !filterDto.getServiceLines().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.serviceLines),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getServiceLines()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getInteriorFinishing()) && !filterDto.getInteriorFinishing().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.interiorFinishing),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getInteriorFinishing()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMortgageIsPossible()) && !filterDto.getMortgageIsPossible().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.mortgageIsPossible),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getMortgageIsPossible()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(ApartmentDailyRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.APARTMENTS_DAILY_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.constructionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNewConstruction()) && !filterDto.getNewConstruction().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.newConstruction),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNewConstruction()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTheHouseHas()) && !filterDto.getTheHouseHas().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.theHouseHas),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getTheHouseHas()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.floorsInTheBuilding),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloor()) && !filterDto.getStartFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floor),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloor()) && !filterDto.getEndFloor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floor),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getParking()) && !filterDto.getParking().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.parking),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getParking()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.amenities),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.appliances),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWindowViews()) && !filterDto.getWindowViews().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.windowViews),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWindowViews()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.withPets),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWithPets()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithChildren()) && !filterDto.getWithChildren().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.withChildren),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWithChildren()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfGuests()) && !filterDto.getNumberOfGuests().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
					 FilterConstants.numberOfGuests),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNumberOfGuests()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.renovation),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBalcony()) && !filterDto.getBalcony().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.balcony),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getBalcony()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getElevator()) && !filterDto.getElevator().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.elevator),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getElevator()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCeilingHeight()) && !filterDto.getCeilingHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.ceilingHeight),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getCeilingHeight()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.numberOfRooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.numberOfBathrooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getComfort()) && !filterDto.getComfort().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.comfort),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getComfort()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(HouseDailyRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.HOUSES_DAILY_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConstructionType()) && !filterDto.getConstructionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.constructionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getConstructionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFloorsInTheBuilding()) && !filterDto.getFloorsInTheBuilding().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.floorsInTheBuilding),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getFloorsInTheBuilding()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartHouseArea()) && !filterDto.getStartHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.houseArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndHouseArea()) && !filterDto.getEndHouseArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.houseArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndHouseArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGarage()) && !filterDto.getGarage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.garage),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGarage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAmenities()) && !filterDto.getAmenities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.amenities),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAmenities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAppliances()) && !filterDto.getAppliances().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.appliances),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAppliances()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.withPets),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWithPets()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithChildren()) && !filterDto.getWithChildren().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.withChildren),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWithChildren()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfGuests()) && !filterDto.getNumberOfGuests().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
					 FilterConstants.numberOfGuests),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNumberOfGuests()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRenovation()) && !filterDto.getRenovation().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.renovation),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getRenovation()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfRooms()) && !filterDto.getNumberOfRooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.numberOfRooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getNumberOfRooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfBathrooms()) && !filterDto.getNumberOfBathrooms().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.numberOfBathrooms),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNumberOfBathrooms()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getComfort()) && !filterDto.getComfort().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.comfort),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getComfort()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(MobilePhoneFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.MOBILE_PHONES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getModel()) && !filterDto.getModel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.model),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getModel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStorage()) && !filterDto.getStorage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.storage),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getStorage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(NotebookFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.NOTEBOOKS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMemory()) && !filterDto.getMemory().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.memory),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMemory()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMemoryRAM()) && !filterDto.getMemoryRAM().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.memoryRam),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMemoryRAM()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getProcessor()) && !filterDto.getProcessor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.processor),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getProcessor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenSize()) && !filterDto.getScreenSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.screenSize),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getScreenSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenResolution()) && !filterDto.getScreenResolution().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.screenResolution),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getScreenResolution()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(ComputerFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.COMPUTERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMemory()) && !filterDto.getMemory().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.memory),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMemory()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMemoryRAM()) && !filterDto.getMemoryRAM().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.memoryRam),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMemoryRAM()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getProcessor()) && !filterDto.getProcessor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.processor),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getProcessor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenSize()) && !filterDto.getScreenSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.screenSize),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getScreenSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenResolution()) && !filterDto.getScreenResolution().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.screenResolution),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getScreenResolution()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(SmartWatchFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SMART_WATCHES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(TabletFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TABLETS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenSize()) && !filterDto.getScreenSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.screenSize),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getScreenSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMemory()) && !filterDto.getMemory().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.memory),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMemory()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(TVFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TV_STREAMERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getScreenSize()) && !filterDto.getScreenSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.screenSize),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getScreenSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(GamingConsoleFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GAMING_CONSOLES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(HeadphoneFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.HEADPHONES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getConnectionType()) && !filterDto.getConnectionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
					 FilterConstants.connectionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getConnectionType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(ComputerAndNotebookPartsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.COMPUTER_AND_NOTEBOOK_PARTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(PhotoAndVideoCameraFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PHOTO_AND_VIDEO_CAMERAS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(ComputerGamesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.COMPUTER_GAMES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(SmartHomeAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SMART_HOME_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(WasherFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WASHERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMaximumLaundryCapacity()) && !filterDto.getMaximumLaundryCapacity().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.maximumLaundryCapacity),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getMaximumLaundryCapacity()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getLaundryLoadType()) && !filterDto.getLaundryLoadType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.laundryLoadType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getLaundryLoadType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(ClothesDryerFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CLOTHES_DRYERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(IronAndAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.IRONS_AND_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(RefrigeratorFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.REFRIGERATORS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(FreezerFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.FREEZERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(DishwasherFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.DISHWASHERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(MicrowaveFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.MICROWAVES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(StoveFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.STOVES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getBurnerType()) && !filterDto.getBurnerType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.burnerType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getBurnerType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(CoffeeMakerAndAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.COFFEE_MAKERS_AND_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(KettleFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.KETTLES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(RangeHoodFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.RANGE_HOODS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(VacuumCleanerFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.VACUUM_CLEANERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(RoboticVacuumFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.ROBOTIC_VACUUMS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(FloorWasherFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.FLOOR_WASHERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(AirConditionerFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.AIR_CONDITIONERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(WaterHeatersFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WATER_HEATERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(AirPurifiersAndHumidifiersFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.AIR_PURIFIERS_AND_HUMIDIFIERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(ComputerPeripheralFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.COMPUTERS_PERIPHERALS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(AudioPlayerAndStereoFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.AUDIO_PLAYERS_AND_STEREOS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(QuadcoptersAndDronesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.QUADCOPTERS_AND_DRONES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(SofaAndArmchairFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.QUADCOPTERS_AND_DRONES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getUpholstery()) && !filterDto.getUpholstery().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.upholstery),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getUpholstery()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(StorageFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.STORAGE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(TableAndChairFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TABLES_AND_CHAIRS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(BedroomFurnitureFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BEDROOM_FURNITURE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(KitchenFurnitureFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.KITCHEN_FURNITURE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(GardenFurnitureFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GARDEN_FURNITURE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(BarbecueAndAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BARBECUE_AND_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(GardenDecorFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GARDEN_DECOR);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(GardenAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GARDEN_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(LightingFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.LIGHTING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(TextileFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TEXTILES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(RugFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.RUGS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRugWidth()) && !filterDto.getRugWidth().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.rugWidth),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getRugWidth()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRugLength()) && !filterDto.getRugLength().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.rugLength),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getRugLength()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(InteriorDecorationFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.INTERIOR_DECORATION);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(TablewareFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TABLEWARE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(CookingAndBakingFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.COOKING_AND_BAKING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(KitchenAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.KITCHEN_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(BathroomAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BATHROOM_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(VideoSurveillanceFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.VIDEO_SURVEILLANCE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(CarPartFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CAR_PARTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getOriginality()) && !filterDto.getOriginality().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.originality),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getOriginality()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPartSide()) && !filterDto.getPartSide().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.partSide),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getPartSide()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getPartPosition()) && !filterDto.getPartPosition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.partPosition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getPartPosition()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(WheelAndTireFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WHEELS_AND_TIRES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSeason()) && !filterDto.getSeason().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.season),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getSeason()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWidth()) && !filterDto.getWidth().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.width),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWidth()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getHeight()) && !filterDto.getHeight().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.height),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getHeight()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getDiameter()) && !filterDto.getDiameter().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.diameter),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getDiameter()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(RimAndHubCapFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.RIMS_AND_HUB_CAPS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getDiameter()) && !filterDto.getDiameter().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.diameter),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getDiameter()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(CarBatteryFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CAR_BATTERIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getVoltage()) && !filterDto.getVoltage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.voltage),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getVoltage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartCapacity()) && !filterDto.getStartCapacity().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.capacity),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartCapacity()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndCapacity()) && !filterDto.getEndCapacity().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.capacity),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndCapacity()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(GasEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GAS_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(OilAndChemicalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.OILS_AND_CHEMICALS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(CarAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CAR_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(CarElectronicFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CAR_ELECTRONICS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(CarAudioAndVideoFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CAR_AUDIO_AND_VIDEO);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(PersonalTransportationFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PERSONAL_TRANSPORTATION);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(AtvAndSnowmobileFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.ATVS_AND_SNOWMOBILES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(BoatAndWaterTransportFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BOATS_AND_WATER_TRANSPORT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(TrailerAndBoothFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TRAILERS_AND_BOOTHS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getExteriorFinish()) && !filterDto.getExteriorFinish().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.exteriorFinishing),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getExteriorFinish()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(EventVenueRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.EVENT_VENUES_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartFloorArea()) && !filterDto.getStartFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndFloorArea()) && !filterDto.getEndFloorArea().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.floorArea),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndFloorArea()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfGuests()) && !filterDto.getNumberOfGuests().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
					 FilterConstants.numberOfGuests),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNumberOfGuests()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEventTypes()) && !filterDto.getEventTypes().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.eventTypes),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getEventTypes()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEquipment()) && !filterDto.getEquipment().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.equipment),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getEquipment()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getFacilities()) && !filterDto.getFacilities().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.facilities),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getFacilities()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNoiseAfterHours()) && !filterDto.getNoiseAfterHours().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName),
							FilterConstants.noiseAfterHours),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getNoiseAfterHours()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getWithPets()) && !filterDto.getWithPets().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.withPets),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getWithPets()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WomenClothingBuyFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WOMEN_CLOTHING_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSize()) && !filterDto.getSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.size),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WomenClothingRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WOMEN_CLOTHING_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSize()) && !filterDto.getSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.size),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WomenShoesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WOMEN_SHOES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSeason()) && !filterDto.getSeason().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.season),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getSeason()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getShoeSize()) && !filterDto.getShoeSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.shoeSize),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getShoeSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WomenAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WOMEN_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(MenClothingBuyFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.MEN_CLOTHING_BUY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSize()) && !filterDto.getSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.size),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(MenShoesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.MEN_SHOES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSeason()) && !filterDto.getSeason().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.season),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getSeason()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getShoeSize()) && !filterDto.getShoeSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.shoeSize),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getShoeSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(MenAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.MEN_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(JewelleryFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.JEWELLERY);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(GlassesAndFramesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GLASSES_AND_FRAMES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WatchesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WATCHES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getClockFace()) && !filterDto.getClockFace().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.clockFace),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getClockFace()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(HandbagsAndWalletsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.HANDBAGS_AND_WALLETS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMaterial()) && !filterDto.getMaterial().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.material),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMaterial()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WorkwearAndAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WORKWEAR_AND_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSize()) && !filterDto.getSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.size),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(CarnivalCostumesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CARNIVAL_COSTUMES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSize()) && !filterDto.getSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.size),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WeddingDressesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WEDDING_DRESSES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSize()) && !filterDto.getSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.size),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WeddingShoesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WEDDING_SHOES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getShoeSize()) && !filterDto.getShoeSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.shoeSize),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getShoeSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WeddingAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WEDDING_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(CollectibleItemsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.COLLECTIBLE_ITEMS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(PaintingsAndPicturesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PAINTINGS_AND_PICTURES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ArtsObjectsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.ARTS_OBJECTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ArtsAndCraftsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.ARTS_AND_CRAFTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(MotorcycleFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.MOTORCYCLES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartYear()) && !filterDto.getStartYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.year),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndYear()) && !filterDto.getEndYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.year),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartEngineSize()) && !filterDto.getStartEngineSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.engineSize),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartEngineSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndEngineSize()) && !filterDto.getEndEngineSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.engineSize),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndEngineSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getTransmission()) && !filterDto.getTransmission().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.transmission),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getTransmission()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEngineType()) && !filterDto.getEngineType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.engineType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getEngineType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartMileage()) && !filterDto.getStartMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mileage),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndMileage()) && !filterDto.getEndMileage().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mileage),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndMileage()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getColor()) && !filterDto.getColor().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.color),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getColor()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<ItemEntity> filterItems(MotorcyclePartsAndAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.MOTORCYCLE_PARTS_AND_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(GuitarsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GUITARS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(PianosAndKeyboardInstrumentsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PIANOS_AND_KEYBOARD_INSTRUMENTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BrassAndWoodwindInstrumentsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BRASS_AND_WOODWIND_INSTRUMENTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(StringInstrumentsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.STRING_INSTRUMENTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(AccordionsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.ACCORDIONS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(DrumsAndPercussionInstrumentsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.DRUMS_AND_PERCUSSION_INSTRUMENTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(StudioAccessoriesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.STUDIO_ACCESSORIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(HuntingAndFishingFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.HUNTING_AND_FISHING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(CampingEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CAMPING_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(FitnessAndExerciseEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.FITNESS_AND_EXERCISE_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BilliardAndBowlingFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BILLIARD_AND_BOWLING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(FootballAndBallGamesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.FOOTBALL_AND_BALL_GAMES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WaterSportsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WATER_SPORTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WinterSportsEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WINTER_SPORTS_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BoxingAndMartialArtsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BOXING_AND_MARTIAL_ARTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(TennisAndBadmintonFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TENNIS_AND_BADMINTON);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(MountaineeringFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.MOUNTAINEERING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(SportsNutritionFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SPORTS_NUTRITION);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BooksAndMagazinesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BOOKS_AND_MAGAZINES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(FilmsAndMusicFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.FILMS_AND_MUSIC);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(DogsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.DOGS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAge()) && !filterDto.getAge().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.age),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAge()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(CatsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CATS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAge()) && !filterDto.getAge().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.age),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAge()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(FishFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.FISH);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNutritionType()) && !filterDto.getNutritionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.nutritionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getNutritionType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BirdsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BIRDS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNutritionType()) && !filterDto.getNutritionType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.nutritionType),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getNutritionType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(RodentsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.RODENTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ReptilesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.REPTILES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(CattleFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CATTLE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAge()) && !filterDto.getAge().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.age),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAge()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(HorsesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.HORSES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAge()) && !filterDto.getAge().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.age),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAge()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(PigsAndPigletsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PIGS_AND_PIGLETS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAge()) && !filterDto.getAge().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.age),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAge()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(SheepAndGoatFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SHEEP_AND_GOAT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAge()) && !filterDto.getAge().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.age),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAge()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(RabbitsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.RABBITS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getGender()) && !filterDto.getGender().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.gender),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getGender()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getAge()) && !filterDto.getAge().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.age),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getAge()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(GirlsClothingFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GIRLS_CLOTHING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BoysClothingFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BOYS_CLOTHING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BabiesClothingFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BABIES_CLOTHING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ShoesForGirlsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SHOES_FOR_GIRLS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSeason()) && !filterDto.getSeason().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.season),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getSeason()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getShoeSize()) && !filterDto.getShoeSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.shoeSize),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getShoeSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ShoesForBoysFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SHOES_FOR_BOYS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getSeason()) && !filterDto.getSeason().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.season),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getSeason()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getShoeSize()) && !filterDto.getShoeSize().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.shoeSize),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getShoeSize()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(KidsTransportationFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.KIDS_TRANSPORTATION);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BuildingSetsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BUILDING_SETS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(LearningAndEducationalToysFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.LEARNING_AND_EDUCATIONAL_TOYS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ToysForGirlsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TOYS_FOR_GIRLS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ToysForBoysFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TOYS_FOR_BOYS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ToysForNewbornsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TOYS_FOR_NEWBORNS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(OutdoorsAndSeasonalToysFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.OUTDOORS_AND_SEASONAL_TOYS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ProductsForBabiesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PRODUCTS_FOR_BABIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(StrollersFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.STROLLERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getForAges()) && !filterDto.getForAges().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.forAges),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getForAges()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getNumberOfSeats()) && !filterDto.getNumberOfSeats().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.numberOfSeats),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng),
					 filterDto.getNumberOfSeats()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(CarSeatsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CAR_SEATS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BabyCarriersFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BABY_CARRIERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WalkersFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WALKERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(SwingsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SWINGS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(PlaypensFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PLAYPENS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BedAccessoriesAndDecorFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BED_ACCESSORIES_AND_DECOR);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(KidsFurnitureFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.KIDS_FURNITURE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(HighChairsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.HIGH_CHAIRS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(FeedingFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.FEEDING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BathAndHygieneFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BATH_AND_HYGIENE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BackpacksAndBagsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BACKPACKS_AND_BAGS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(SchoolSuppliesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SCHOOL_SUPPLIES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ProductsForDogsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PRODUCTS_FOR_DOGS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ProductsForCatsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PRODUCTS_FOR_CATS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ProductsForFishAndReptilesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PRODUCTS_FOR_FISH_AND_REPTILES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ProductsForRodentsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PRODUCTS_FOR_RODENTS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ProductsForFarmAnimalsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PRODUCTS_FOR_FARM_ANIMALS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ProductsForBirdsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PRODUCTS_FOR_BIRDS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(HandToolsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.HAND_TOOLS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(MachineToolsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.MACHINE_TOOLS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ElectricalDevicesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.ELECTRICAL_DEVICES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(MeasuringEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.MEASURING_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(SawsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SAWS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WeldingEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WELDING_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ConcreteMixersFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CONCRETE_MIXERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(LaddersAndStepladdersFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.LADDERS_AND_STEPLADDERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ScaffoldingAndTowersFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SCAFFOLDING_AND_TOWERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ProtectiveEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PROTECTIVE_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(GardeningEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GARDENING_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(FaucetsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.FAUCETS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ShowersAndBathsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SHOWERS_AND_BATHS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(SinksAndWashbasinsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.SINKS_AND_WASHBASINS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(PumpsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.PUMPS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WaterMetersFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WATER_METERS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ToiletsAndBidetsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.TOILETS_AND_BIDETS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WindowsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WINDOWS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(DoorsFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.DOORS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(GatesAndFencesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.GATES_AND_FENCES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(FlooringFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.FLOORING);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WaterSupplyAndPipesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WATER_SUPPLY_AND_PIPES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(RetailAndShopsEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.RETAIL_AND_SHOPS_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(OfficeEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.OFFICE_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(ManufacturingEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.MANUFACTURING_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(RestaurantsAndCafesEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.RESTAURANTS_AND_CAFES_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BeautySalonsEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BEAUTY_SALONS_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(CarServicesEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CAR_SERVICES_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(AgriculturalEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.AGRICULTURAL_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(WarehouseEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.WAREHOUSE_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(AttractionsAndVendingMachinesFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.ATTRACTIONS_AND_VENDING_MACHINES);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(AdvertisingAndExhibitionEquipmentFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.ADVERTISING_AND_EXHIBITION_EQUIPMENT);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCondition()) && !filterDto.getCondition().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.condition),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getCondition()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BusinessesSaleFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BUSINESSES_SALE);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(BusinessesRentalFilterDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.BUSINESSES_RENTAL);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getStartPrice()) && !filterDto.getStartPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getStartPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndPrice()) && !filterDto.getEndPrice().isEmpty()) {
			Predicate predicate =
					criteriaBuilder.lessThanOrEqualTo(itemRoot.get(FilterConstants.price).get(FilterConstants.price),
							filterDto.getEndPrice());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCurrency())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.price).get(FilterConstants.currency),
							filterDto.getCurrency());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCountry())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.country),
							filterDto.getCountry());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getRegion())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.region),
							filterDto.getRegion());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getCity())) {
			Predicate predicate =
					criteriaBuilder.equal(itemRoot.get(FilterConstants.location).get(FilterConstants.city),
							filterDto.getCity());
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getType()) && !filterDto.getType().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.type),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getType()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> filterItems(PriceStatisticsRequestDto filterDto) {
		List<Predicate> predicates = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ItemEntity> criteriaQuery = criteriaBuilder.createQuery(ItemEntity.class);
		Root<ItemEntity> itemRoot = criteriaQuery.from(ItemEntity.class);

		Predicate categoryPredicate =
				criteriaBuilder.equal(itemRoot.get(FilterConstants.category).get(FilterConstants.name),
						CategoryEnum.CARS);
		predicates.add(categoryPredicate);

		if (nonNull(filterDto.getMark()) && !filterDto.getMark().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.mark),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getMark()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getModel()) && !filterDto.getModel().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.model),
					criteriaBuilder.equal(itemFieldJoin.get(FilterConstants.fieldValue_eng), filterDto.getModel()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getStartYear()) && !filterDto.getStartYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.year),
					criteriaBuilder.greaterThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getStartYear()));
			predicates.add(predicate);
		}

		if (nonNull(filterDto.getEndYear()) && !filterDto.getEndYear().isEmpty()) {
			Join<ItemEntity, FieldEntity> itemFieldJoin = itemRoot.join(FilterConstants.fields, JoinType.INNER);
			Join<FieldEntity, FieldNameEntity> fieldNameJoin =
					itemFieldJoin.join(FilterConstants.fieldName, JoinType.INNER);
			Predicate predicate = criteriaBuilder.and(
					criteriaBuilder.equal(fieldNameJoin.get(FilterConstants.fieldName), FilterConstants.year),
					criteriaBuilder.lessThanOrEqualTo(itemFieldJoin.get(FilterConstants.fieldValue_eng),
							filterDto.getEndYear()));
			predicates.add(predicate);
		}

		Predicate combinedPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		criteriaQuery.select(itemRoot).where(combinedPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<ItemEntity> findItemsByCategory(Long categoryId) {
		return itemRepository.findByCategoryId(categoryId);
	}

	private ItemEntity updateEntity(ItemEntity itemEntity, ItemUpdateDto itemUpdateDto) {

		itemEntity.setTitle(isNull(itemUpdateDto.getTitle()) ? itemEntity.getTitle() : itemUpdateDto.getTitle());
		itemEntity.setDescription(
				isNull(itemUpdateDto.getDescription()) ? itemEntity.getDescription() : itemUpdateDto.getDescription());
		itemEntity.setPrice(Price.builder()
				.price(isNull(itemUpdateDto.getPrice()) ? itemEntity.getPrice().getPrice() : itemUpdateDto.getPrice())
				.currency(isNull(itemUpdateDto.getCurrency()) ? itemEntity.getPrice().getCurrency() :
						itemUpdateDto.getCurrency())
				.build());

		itemEntity.setFields(valueService.updateValues(itemEntity.getFields(), itemUpdateDto.getFieldsValue()));

		itemEntity.setCity(isNull(itemUpdateDto.getCityId()) ? itemEntity.getCity() :
				locationService.getCityById(itemUpdateDto.getCityId()));
		itemEntity.setAddress(isNull(itemUpdateDto.getAddress()) ? itemEntity.getAddress() :
				itemUpdateDto.getAddress());
		itemEntity.setLat(isNull(itemUpdateDto.getLat()) ? itemEntity.getLat() :
				itemUpdateDto.getLat());
		itemEntity.setLon(isNull(itemUpdateDto.getLon()) ? itemEntity.getLon() :
				itemUpdateDto.getLon());
		itemEntity.setUpdatedAt(ZonedDateTime.now());
		return itemRepository.save(itemEntity);
	}

	private ItemEntity saveOrChangePriceHistory(ItemEntity updatedEntity) {
		if (isNull(updatedEntity.getPriceHistories())) {
			updatedEntity.setPriceHistories(new ArrayList<>());
		}

		if (!updatedEntity.getPriceHistories().isEmpty()) {
			PriceHistoryEntity lastPriceHistory = updatedEntity.getPriceHistories()
					.stream()
					.max(Comparator.comparing(PriceHistoryEntity::getStartDate))
					.get();

			lastPriceHistory.setEndDate(LocalDateTime.now());
			priceHistoryRepository.save(lastPriceHistory);
		}

		PriceHistoryEntity newPriceHistory = PriceHistoryEntity.builder()
				.item(updatedEntity)
				.price(updatedEntity.getPrice().getPrice())
				.startDate(LocalDateTime.now())
				.build();
		updatedEntity.getPriceHistories().add(newPriceHistory);
		priceHistoryRepository.save(newPriceHistory);

		return itemRepository.save(updatedEntity);
	}

	private ItemEntity initialPriceHistoryForNewItem(ItemEntity savedEntity) {
		PriceHistoryEntity initialPriceHistory = PriceHistoryEntity.builder()
				.item(savedEntity)
				.price(savedEntity.getPrice().getPrice())
				.startDate(LocalDateTime.now())
				.build();

		savedEntity.setPriceHistories(new ArrayList<>());
		savedEntity.getPriceHistories().add(initialPriceHistory);
		priceHistoryRepository.save(initialPriceHistory);
		return savedEntity;
	}

	private ItemEntity getItemByIdOrElseThrow(Long id) {
		return itemRepository.findById(id).orElseThrow(() -> new NotFoundException(ExceptionConstants.ITEM_NOT_FOUND));
	}

}