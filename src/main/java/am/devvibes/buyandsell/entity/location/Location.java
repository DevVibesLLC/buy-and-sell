package am.devvibes.buyandsell.entity.location;

import am.devvibes.buyandsell.util.LocationEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {

	@Column(nullable = false)
	private String country;

	@Column(nullable = false)
	private String region;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false)
	private Double lat;

	@Column(nullable = false)
	private Double lon;

}
