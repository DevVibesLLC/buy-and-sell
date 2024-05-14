package am.devvibes.buyandsell.model.dto.product;

import am.devvibes.buyandsell.util.Category;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponseDto {

	private Long id;
	private String name;
	private String description;
	private Category category;
	private Double price;
	private Integer quantity;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
