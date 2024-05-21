package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

}