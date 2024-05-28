package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.MeasurementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementRepository extends JpaRepository<MeasurementEntity, Long> {

}