package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.CategoryEntity;
import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

	List<AutoMarkEntity> findMarksById(Long categoryId);
}