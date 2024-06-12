package am.devvibes.buyandsell.repository.field;

import am.devvibes.buyandsell.entity.FieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends JpaRepository<FieldEntity, Long> {

}