package am.devvibes.buyandsell.dto.itemForSell;

import am.devvibes.buyandsell.util.Category;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ItemForSellResponseDto {

	private Long id;
	private String name;
	private String description;
	private Category category;
	private Double price;
	private Integer quantity;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
