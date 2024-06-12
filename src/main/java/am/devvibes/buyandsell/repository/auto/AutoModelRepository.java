package am.devvibes.buyandsell.repository.auto;

import am.devvibes.buyandsell.entity.auto.AutoModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoModelRepository extends JpaRepository<AutoModelEntity, Long> {

}