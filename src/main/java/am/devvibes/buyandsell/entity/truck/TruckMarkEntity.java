package am.devvibes.buyandsell.entity.truck;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TruckMarkEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@OneToMany(mappedBy = "truckMark", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<TruckModelEntity> models;

}