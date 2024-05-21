package am.devvibes.buyandsell.dto.itemForSell;

import am.devvibes.buyandsell.entity.CategoryEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemForSellRequestDto {

	@NotBlank
	private String name;
	@NotBlank
	private String description;
	@NotNull
	private CategoryEntity category;
	@NotNull
	private Double price;
	@NotNull
	private Integer quantity;

}
