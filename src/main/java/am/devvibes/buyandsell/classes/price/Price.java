package am.devvibes.buyandsell.classes.price;

import am.devvibes.buyandsell.util.CurrencyEnum;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Price {

	private BigDecimal price;
	private CurrencyEnum currency;

}
