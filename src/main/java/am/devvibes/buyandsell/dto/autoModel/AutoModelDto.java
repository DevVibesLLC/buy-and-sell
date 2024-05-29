package am.devvibes.buyandsell.dto.autoModel;

import lombok.*;
import org.springframework.context.annotation.Bean;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutoModelDto {

	private Long id;
	private String model;

}
