package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.CategoryEntity;
import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AutoMarkRepository extends JpaRepository<AutoMarkEntity, Long> {

	@Query("SELECT c.models FROM AutoMarkEntity c WHERE c.id = :markId")
	List<AutoModelEntity> findModelsById(Long markId);



}