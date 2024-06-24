package am.devvibes.buyandsell.dto.item;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.value.FieldValuesDto;
import am.devvibes.buyandsell.entity.location.Location;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemResponseDto {

	private Long id;

	private String title;

	private String description;

	private Price price;

	private Location location;

	private String userId;

	private List<FieldValuesDto> fields;

	private List<String> imgUrls;

}
