package am.devvibes.buyandsell.dto.item;

import am.devvibes.buyandsell.dto.value.FieldValuesDto;
import am.devvibes.buyandsell.util.CurrencyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemUpdateDto {

	private String title;

	private String description;

	private BigDecimal price;

	private CurrencyEnum currency;

	private List<FieldValuesDto> fieldsValue;

	private String address;

	private Long cityId;

	private Double lat;

	private Double lon;

}
