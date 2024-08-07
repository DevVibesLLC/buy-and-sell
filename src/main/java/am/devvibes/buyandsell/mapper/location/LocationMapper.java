package am.devvibes.buyandsell.mapper.location;

import am.devvibes.buyandsell.dto.city.CityDto;
import am.devvibes.buyandsell.dto.country.CountryDto;
import am.devvibes.buyandsell.dto.region.RegionDto;
import am.devvibes.buyandsell.entity.city.CityEntity;
import am.devvibes.buyandsell.entity.country.CountryEntity;
import am.devvibes.buyandsell.entity.region.RegionEntity;

import java.util.List;

public interface LocationMapper {

	CountryDto mapCountryEntityToDto(CountryEntity countryEntity);

	RegionDto mapRegionEntityToDto(RegionEntity regionEntity);

	CityDto mapCityEntityToDto(CityEntity cityEntity);

	List<CountryDto> mapCountryEntityListToDtoList(List<CountryEntity> countryEntityList);

	List<RegionDto> mapRegionEntityListToDtoList(List<RegionEntity> regionEntityList);

	List<CityDto> mapCityEntityListToDtoList(List<CityEntity> cityEntityList);

}
