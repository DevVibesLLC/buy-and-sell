package am.devvibes.buyandsell.entity.mobile;

import am.devvibes.buyandsell.entity.bus.BusModelEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobilePhoneMarkEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@OneToMany(mappedBy = "mobilePhoneMark", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<MobilePhoneModelEntity> models;

}
