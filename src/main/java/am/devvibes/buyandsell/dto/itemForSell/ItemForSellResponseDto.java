package am.devvibes.buyandsell.dto.itemForSell;

import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.entity.CategoryEntity;
import am.devvibes.buyandsell.entity.UserEntity;
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
	private CategoryDto category;
	private String userId;
	private Double price;
	private Integer quantity;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
