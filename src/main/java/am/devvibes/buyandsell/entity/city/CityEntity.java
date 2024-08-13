package am.devvibes.buyandsell.entity.city;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.entity.region.RegionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CityEntity  extends BaseEntity {

    @Column(nullable = false)
    private String name_eng;

    @Column(nullable = false)
    private String name_ru;

    @Column(nullable = false)
    private String name_hy;

    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private RegionEntity region;

}