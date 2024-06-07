package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.FieldNameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends JpaRepository<FieldNameEntity, Long> {

}