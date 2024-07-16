package am.devvibes.buyandsell.dto.priceHistory;

import am.devvibes.buyandsell.entity.item.ItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceHistoryDto {

	private BigDecimal price;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

}
