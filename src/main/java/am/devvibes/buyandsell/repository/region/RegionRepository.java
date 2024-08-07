package am.devvibes.buyandsell.repository.region;

import am.devvibes.buyandsell.entity.region.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<RegionEntity, Long> {

	Optional<List<RegionEntity>> findByCountryId(Long countryId);

}