package am.devvibes.buyandsell.dto.vehicle.vehicleModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleModelDto {

	@NotNull
	private Long id;

	@NotBlank
	private String model;

}
