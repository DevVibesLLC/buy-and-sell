package am.devvibes.buyandsell.model.entity;

import am.devvibes.buyandsell.model.entity.abstracts.AbstractProduct;
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
@Table(name = "products")
public class ItemForSell extends AbstractProduct {

	@Builder
	public ItemForSell(Long id,
			String name,
			String description,
			Category category,
			Double price,
			Integer quantity,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		super(id, name, description, category, price, quantity, createdAt, updatedAt);

	}

}
