package am.devvibes.buyandsell.entity.region;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.entity.city.CityEntity;
import am.devvibes.buyandsell.entity.country.CountryEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RegionEntity extends BaseEntity {

	@Column(nullable = false)
	private String name_eng;

	@Column(nullable = false)
	private String name_ru;

	@Column(nullable = false)
	private String name_hy;

	@ManyToOne
	@JoinColumn(name = "country_id", nullable = false)
	private CountryEntity country;

	@OneToMany(mappedBy = "region", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<CityEntity> cities;

}