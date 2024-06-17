package am.devvibes.buyandsell.repository.auto;

import am.devvibes.buyandsell.entity.auto.AutoMarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoMarkRepository extends JpaRepository<AutoMarkEntity, Long> {

}