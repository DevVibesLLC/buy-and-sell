package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.ValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValueRepository extends JpaRepository<ValueEntity, Long> {

}