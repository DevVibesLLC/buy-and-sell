package am.devvibes.buyandsell.repository.field;

import am.devvibes.buyandsell.entity.FieldNameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldNameRepository extends JpaRepository<FieldNameEntity, Long> {

}