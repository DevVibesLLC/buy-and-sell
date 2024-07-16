package am.devvibes.buyandsell.repository.priceHistory;

import am.devvibes.buyandsell.entity.priceHistory.PriceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistoryEntity, Long> {

}