package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.auto.GenerationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenerationRepository extends JpaRepository<GenerationEntity, Long> {

}