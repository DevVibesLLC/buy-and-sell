package am.devvibes.buyandsell.classes;

import am.devvibes.buyandsell.util.CurrencyEnum;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;
import java.util.Currency;

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
