package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.entity.abstracts.AbstractItemForSell;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

//https://www.gumtree.com/
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "items_for_sell")
public class ItemForSellEntity extends AbstractItemForSell {
	@OneToOne
	private UserEntity userEntity;
	@Builder
	public ItemForSellEntity(Long id,
			String name,
			String description,
			Double price,
			Integer quantity,
			CategoryEntity category,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		super(id, name, description, price, quantity, category, createdAt, updatedAt);

	}

}
