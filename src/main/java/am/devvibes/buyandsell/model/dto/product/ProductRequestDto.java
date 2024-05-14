package am.devvibes.buyandsell.model.dto.product;

import am.devvibes.buyandsell.util.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductRequestDto {

	@NotBlank
	private String name;
	@NotBlank
	private String description;
	@NotBlank
	private Category category;
	@NotNull
	private Double price;
	@NotNull
	private Integer quantity;

}
