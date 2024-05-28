package am.devvibes.buyandsell.mapper;

import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.entity.FieldEntity;
import am.devvibes.buyandsell.entity.ItemEntity;
import org.springframework.stereotype.Service;


public interface FieldMapper {

	FieldEntity mapDtoToEntity(FieldRequestDto fieldRequestDto);

}
