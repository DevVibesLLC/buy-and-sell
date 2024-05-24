package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum CurrencyEnum {
	AMD("AMD"),
	USD("USD");

	private final String currency;

	CurrencyEnum(String currency) {
		this.currency = currency;
	}
}