package am.devvibes.buyandsell.dto.item;

import am.devvibes.buyandsell.dto.FieldValuesDto;
import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.util.CurrencyEnum;
import jakarta.persistence.*;
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

	private String title;

	private String description;

	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	private CurrencyEnum currency;

	private List<FieldValuesDto> fieldsValue;

	private String address;

	private Long cityId;

	private String imgUrl;

}
