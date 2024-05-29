package am.devvibes.buyandsell.dto.item;

import am.devvibes.buyandsell.classes.Price;
import am.devvibes.buyandsell.dto.FieldValuesDto;
import am.devvibes.buyandsell.dto.category.CategoryDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.entity.CategoryEntity;
import am.devvibes.buyandsell.entity.Location;
import am.devvibes.buyandsell.entity.UserEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemResponseDto {

	private String title;

	private String description;

	private Price price;

	private Location location;

	private String userId;

	private List<FieldValuesDto> fields;

	private String imgUrl;

}
