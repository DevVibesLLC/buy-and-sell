package am.devvibes.buyandsell.entity.location;

import am.devvibes.buyandsell.util.LocationEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {

	@Column(nullable = false)
	private LocationEnum country;

	@Column(nullable = false)
	private LocationEnum region;

	@Column(nullable = false)
	private LocationEnum city;

	@Column(nullable = false)
	private String address;

}
