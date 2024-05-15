package am.devvibes.buyandsell.entity;

import am.devvibes.buyandsell.entity.abstracts.AbstractItemForSell;
import am.devvibes.buyandsell.util.Category;
import jakarta.persistence.Entity;
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
public class ItemForSell extends AbstractItemForSell {

	@Builder
	public ItemForSell(Long id,
			String name,
			String description,
			Category category,
			Double price,
			Integer quantity,
			UserEntity user,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		super(id, name, description, category, price, quantity, user, createdAt, updatedAt);

	}

}
