package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutoMarkRepository extends JpaRepository<AutoMarkEntity, Long> {

	List<AutoModelEntity> findModelsById(Long markId);

}