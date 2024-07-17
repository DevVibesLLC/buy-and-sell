package am.devvibes.buyandsell.repository.item;

import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long>, JpaSpecificationExecutor<ItemEntity> {

	List<ItemEntity> findByCategoryId(Long categoryId);

	List<ItemEntity> findByUserEntity(UserEntity userEntity);

	List<ItemEntity> findByUserEntityId(String userId);
}