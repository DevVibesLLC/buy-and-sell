package am.devvibes.buyandsell.dto.item;

import am.devvibes.buyandsell.classes.price.Price;
import am.devvibes.buyandsell.dto.priceHistory.PriceHistoryDto;
import am.devvibes.buyandsell.dto.value.FieldValuesDto;
import am.devvibes.buyandsell.entity.location.Location;
import am.devvibes.buyandsell.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

	private Long businessPageId;

	private Status status;

	private List<FieldValuesDto> fields;

	private List<String> imgUrls;

	private List<String> phoneNumbers;

	private Long countOfViews;

	private Boolean isViewed;

	private List<PriceHistoryDto> priceHistory;

}
