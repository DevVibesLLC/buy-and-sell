package am.devvibes.buyandsell.mapper.priceHistory;

import am.devvibes.buyandsell.dto.priceHistory.PriceHistoryDto;
import am.devvibes.buyandsell.entity.priceHistory.PriceHistoryEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceHistoryMapperImpl implements PriceHistoryMapper{

	@Override
	public PriceHistoryDto mapEntityToDto(PriceHistoryEntity priceHistoryEntity) {
		return PriceHistoryDto.builder()
				.startDate(priceHistoryEntity.getStartDate())
				.endDate(priceHistoryEntity.getEndDate())
				.price(priceHistoryEntity.getPrice())
				.build();
	}

	@Override
	public List<PriceHistoryDto> mapEntityListToDtoList(List<PriceHistoryEntity> priceHistoryEntities) {
		return priceHistoryEntities.stream().map(this::mapEntityToDto).toList();
	}

}
