package am.devvibes.buyandsell.dto.item;

import am.devvibes.buyandsell.dto.value.FieldValuesDto;
import am.devvibes.buyandsell.util.CurrencyEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {

	@NotBlank
	private String title;

	@NotBlank
	private String description;

	@NotNull
	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	private CurrencyEnum currency;

	private List<FieldValuesDto> fieldsValue;

	@NotBlank
	private String address;

	private List<String> imgKeys;

	@NotNull
	private Long cityId;

	@NotNull
	private List<String> phoneNumbers;

}
