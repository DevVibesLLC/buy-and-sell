package am.devvibes.buyandsell.repository.item;

import am.devvibes.buyandsell.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long>, JpaSpecificationExecutor<ItemEntity> {

}