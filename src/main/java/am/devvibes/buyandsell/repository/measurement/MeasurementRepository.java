package am.devvibes.buyandsell.repository.measurement;

import am.devvibes.buyandsell.entity.MeasurementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeasurementRepository extends JpaRepository<MeasurementEntity, Long> {

}