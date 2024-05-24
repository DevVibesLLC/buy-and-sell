package am.devvibes.buyandsell.dto.item;

import am.devvibes.buyandsell.util.CurrencyEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {

	private String name;

	private String description;

	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	private CurrencyEnum currency;

	private String address;

	private Long cityId;

	private String imgUrl;

}
