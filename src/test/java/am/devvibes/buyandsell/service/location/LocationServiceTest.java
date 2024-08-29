package am.devvibes.buyandsell.service.location;

import am.devvibes.buyandsell.entity.city.CityEntity;
import am.devvibes.buyandsell.entity.country.CountryEntity;
import am.devvibes.buyandsell.entity.region.RegionEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.repository.city.CityRepository;
import am.devvibes.buyandsell.repository.country.CountryRepository;
import am.devvibes.buyandsell.repository.region.RegionRepository;
import am.devvibes.buyandsell.util.ExceptionConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

	@Mock
	private CountryRepository countryRepository;

	@Mock
	private RegionRepository regionRepository;

	@Mock
	private CityRepository cityRepository;

	@InjectMocks
	private LocationService locationService;

	@Test
	void getAllCountries__success() {

		CountryEntity armenia = CountryEntity.builder().name_eng("Armenia").build();
		CountryEntity russia = CountryEntity.builder().name_eng("Russia").build();
		CountryEntity usa = CountryEntity.builder().name_eng("USA").build();

		List<CountryEntity> countryEntities = List.of(armenia, russia, usa);

		when(countryRepository.findAll()).thenReturn(countryEntities);

		List<CountryEntity> countries = locationService.getAllCountries();

		assertNotNull(countries);
		assertEquals(countries.size(), countryEntities.size());
		assertTrue(countries.contains(armenia));

		verify(countryRepository, times(1)).findAll();
	}

	@Test
	void getRegionsByCountry__success() {

		Long countryId = 1L;

		RegionEntity ararat = RegionEntity.builder().name_eng("Ararat").build();
		RegionEntity yerevan = RegionEntity.builder().name_eng("Yerevan").build();

		List<RegionEntity> regionEntities = List.of(ararat, yerevan);

		when(regionRepository.findByCountryId(countryId)).thenReturn(Optional.of(regionEntities));

		List<RegionEntity> regions = locationService.getRegionsByCountry(countryId);

		assertNotNull(regions);
		assertEquals(regions.size(), regionEntities.size());

		verify(regionRepository, times(1)).findByCountryId(countryId);

	}

	@Test
	void getRegionsByCountry__regionNotFound() {

		Long countryId = 1L;

		when(regionRepository.findByCountryId(countryId)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> locationService.getRegionsByCountry(countryId), ExceptionConstants.REGION_NOT_FOUND.getString());

		verify(regionRepository, times(1)).findByCountryId(countryId);
	}

	@Test
	void getCitiesByRegion__success() {

		Long regionId = 1L;

		CityEntity erbuni = CityEntity.builder().name_eng("Erebuni").build();
		CityEntity norNork = CityEntity.builder().name_eng("Nor-Nork").build();

		List<CityEntity> cityEntities = List.of(erbuni, norNork);

		when(cityRepository.findByRegionId(regionId)).thenReturn(Optional.of(cityEntities));

		List<CityEntity> cities = locationService.getCitiesByRegion(regionId);

		assertNotNull(cities);
		assertEquals(cities.size(), cityEntities.size());

		verify(cityRepository, times(1)).findByRegionId(regionId);

	}

	@Test
	void getCitiesByRegion__cityNotFound() {

		Long regionId = 1L;

		when(cityRepository.findByRegionId(regionId)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> locationService.getCitiesByRegion(regionId), ExceptionConstants.CITY_NOT_FOUND.getString());

		verify(cityRepository, times(1)).findByRegionId(regionId);
	}

	@Test
	void getCityById__success() {

		Long cityId = 1L;

		CityEntity norNork = CityEntity.builder().name_eng("Nor-Nork").build();
		norNork.setId(cityId);

		when(cityRepository.findById(cityId)).thenReturn(Optional.of(norNork));

		CityEntity cityById = locationService.getCityById(cityId);

		assertNotNull(cityById);
		assertEquals(norNork.getName_eng(), cityById.getName_eng());

		verify(cityRepository, times(1)).findById(cityId);

	}

	@Test
	void getCityById__cityNotFound() {

		Long cityId = 1L;

		when(cityRepository.findById(cityId)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> locationService.getCityById(cityId), ExceptionConstants.CITY_NOT_FOUND.getString());

		verify(cityRepository, times(1)).findById(cityId);

	}



}
