package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoMarkRepository extends JpaRepository<AutoMarkEntity, Long> {

}