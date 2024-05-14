package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.model.entity.ItemForSell;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ItemForSell, Long> {


}