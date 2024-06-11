package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.FieldNameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldNameRepository extends JpaRepository<FieldNameEntity, Long> {

}