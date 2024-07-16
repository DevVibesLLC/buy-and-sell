package am.devvibes.buyandsell.dto.vehicle.vehicleMark;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleMarkDto {

	@NotNull
	private Long id;

	@NotBlank
	private String mark;

}
