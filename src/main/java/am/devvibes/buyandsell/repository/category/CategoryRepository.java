package am.devvibes.buyandsell.repository.category;

import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.util.CategoryEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
	Optional<CategoryEntity> findByName(CategoryEnum name);
}