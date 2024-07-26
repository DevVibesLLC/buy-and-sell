package am.devvibes.buyandsell.dto.vehicle.vehicleMark;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
