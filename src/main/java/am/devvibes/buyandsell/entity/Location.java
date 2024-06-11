package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.util.LocationEnum;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {

	private LocationEnum country;
	private LocationEnum region;
	private LocationEnum city;
	private String address;

}
