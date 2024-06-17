package am.devvibes.buyandsell.entity.bus;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusMarkEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@OneToMany(mappedBy = "busMark", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<BusModelEntity> models;

}
