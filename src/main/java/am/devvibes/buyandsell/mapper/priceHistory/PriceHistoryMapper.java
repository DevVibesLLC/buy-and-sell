package am.devvibes.buyandsell.mapper.priceHistory;

import am.devvibes.buyandsell.dto.priceHistory.PriceHistoryDto;
import am.devvibes.buyandsell.entity.priceHistory.PriceHistoryEntity;

import java.util.List;

public interface PriceHistoryMapper {

	PriceHistoryDto mapEntityToDto(PriceHistoryEntity priceHistoryEntity);

	List<PriceHistoryDto> mapEntityListToDtoList(List<PriceHistoryEntity> priceHistoryEntities);
}
