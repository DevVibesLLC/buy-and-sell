package am.devvibes.buyandsell.dto.category;

import am.devvibes.buyandsell.dto.description.DescriptionDto;
import am.devvibes.buyandsell.util.CategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDto {

	private String categoryName_eng;
	private String categoryName_ru;
	private String categoryName_hy;
	private List<DescriptionDto> descriptions;

}
