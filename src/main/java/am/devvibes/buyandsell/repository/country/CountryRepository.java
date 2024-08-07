package am.devvibes.buyandsell.repository.country;

import am.devvibes.buyandsell.entity.country.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<CountryEntity, Long> {

}