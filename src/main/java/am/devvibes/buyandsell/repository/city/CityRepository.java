package am.devvibes.buyandsell.repository.city;

import am.devvibes.buyandsell.entity.city.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<CityEntity, Long> {

	Optional<List<CityEntity>> findByRegionId(Long regionId);
}