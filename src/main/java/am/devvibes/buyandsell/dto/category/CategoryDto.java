package am.devvibes.buyandsell.dto.category;

import am.devvibes.buyandsell.dto.description.DescriptionRequestDto;
import am.devvibes.buyandsell.util.CategoryEnum;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDto {

	private CategoryEnum name;
	private List<DescriptionRequestDto> descriptions;

}
