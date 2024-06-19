package am.devvibes.buyandsell.entity.mobile;

import am.devvibes.buyandsell.entity.bus.BusMarkEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobilePhoneModelEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "phone_mark_id")
	private MobilePhoneMarkEntity mobilePhoneMark;

}
