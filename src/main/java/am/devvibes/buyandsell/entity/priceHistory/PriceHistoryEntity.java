package am.devvibes.buyandsell.entity.priceHistory;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryEntity extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "item_id", nullable = false)
	private ItemEntity item;

	@Column(nullable = false)
	private BigDecimal price;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

}