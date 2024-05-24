package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

}