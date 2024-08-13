package am.devvibes.buyandsell.mapper.location;

import am.devvibes.buyandsell.dto.city.CityDto;
import am.devvibes.buyandsell.dto.country.CountryDto;
import am.devvibes.buyandsell.dto.region.RegionDto;
import am.devvibes.buyandsell.entity.city.CityEntity;
import am.devvibes.buyandsell.entity.country.CountryEntity;
import am.devvibes.buyandsell.entity.region.RegionEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationMapperImpl implements LocationMapper{

	@Override
	public CountryDto mapCountryEntityToDto(CountryEntity countryEntity) {
		return CountryDto.builder()
				.id(countryEntity.getId())
				.name_eng(countryEntity.getName_eng())
				.name_ru(countryEntity.getName_ru())
				.name_hy(countryEntity.getName_hy())
				.build();
	}

	@Override
	public RegionDto mapRegionEntityToDto(RegionEntity regionEntity) {
		return RegionDto.builder()
				.id(regionEntity.getId())
				.name_eng(regionEntity.getName_eng())
				.name_ru(regionEntity.getName_ru())
				.name_hy(regionEntity.getName_hy())
				.countryId(regionEntity.getCountry().getId())
				.build();
	}

	@Override
	public CityDto mapCityEntityToDto(CityEntity cityEntity) {
		return CityDto.builder()
				.id(cityEntity.getId())
				.name_eng(cityEntity.getName_eng())
				.name_ru(cityEntity.getName_ru())
				.name_hy(cityEntity.getName_hy())
				.regionId(cityEntity.getRegion().getId())
				.build();
	}

	@Override
	public List<CountryDto> mapCountryEntityListToDtoList(List<CountryEntity> countryEntityList) {
		return countryEntityList.stream().map(this::mapCountryEntityToDto).toList();
	}

	@Override
	public List<RegionDto> mapRegionEntityListToDtoList(List<RegionEntity> regionEntityList) {
		return regionEntityList.stream().map(this::mapRegionEntityToDto).toList();
	}

	@Override
	public List<CityDto> mapCityEntityListToDtoList(List<CityEntity> cityEntityList) {
		return cityEntityList.stream().map(this::mapCityEntityToDto).toList();
	}

}