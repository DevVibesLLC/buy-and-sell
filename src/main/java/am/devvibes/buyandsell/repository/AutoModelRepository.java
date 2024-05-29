package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.auto.GenerationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AutoModelRepository extends JpaRepository<AutoModelEntity, Long> {

	@Query("SELECT c.generations FROM AutoModelEntity c WHERE c.id = :modelId")
	List<GenerationEntity> findGenerationsById(Long modelId);

}