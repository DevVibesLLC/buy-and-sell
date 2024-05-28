package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import am.devvibes.buyandsell.entity.auto.GenerationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutoModelRepository extends JpaRepository<AutoModelEntity, Long> {

	List<GenerationEntity> findGenerationsById(Long modelId);

}