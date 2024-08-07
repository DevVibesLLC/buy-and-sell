package am.devvibes.buyandsell.service.location;

import am.devvibes.buyandsell.entity.city.CityEntity;
import am.devvibes.buyandsell.entity.country.CountryEntity;
import am.devvibes.buyandsell.entity.region.RegionEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.repository.city.CityRepository;
import am.devvibes.buyandsell.repository.country.CountryRepository;
import am.devvibes.buyandsell.repository.region.RegionRepository;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final CountryRepository countryRepository;

    private final RegionRepository regionRepository;

    private final CityRepository cityRepository;

    public List<CountryEntity> getAllCountries() {
        return countryRepository.findAll();
    }

    public List<RegionEntity> getRegionsByCountry(Long countryId) {
        return regionRepository.findByCountryId(countryId).orElseThrow(() -> new NotFoundException(
                ExceptionConstants.REGION_NOT_FOUND));
    }

    public List<CityEntity> getCitiesByRegion(Long regionId) {
        return cityRepository.findByRegionId(regionId).orElseThrow(() -> new NotFoundException(
                ExceptionConstants.CITY_NOT_FOUND));
    }

    public CityEntity getCityById(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new NotFoundException(ExceptionConstants.CITY_NOT_FOUND));
    }
}