package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.ValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValueRepository extends JpaRepository<ValueEntity, Long> {

}