package am.devvibes.buyandsell.entity.measurement;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeasurementEntity extends BaseEntity implements Serializable {

    private String symbol;
    private String category;


}