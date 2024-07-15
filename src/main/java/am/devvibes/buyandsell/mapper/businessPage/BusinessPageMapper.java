package am.devvibes.buyandsell.mapper.businessPage;

import am.devvibes.buyandsell.dto.businessPage.BusinessPageRequestDto;
import am.devvibes.buyandsell.dto.businessPage.BusinessPageResponseDto;
import am.devvibes.buyandsell.entity.businessPage.BusinessPageEntity;

public interface BusinessPageMapper {

	BusinessPageResponseDto mapEntityToDto(BusinessPageEntity businessPageEntity);

	BusinessPageEntity mapDtoToEntity(BusinessPageRequestDto businessPageRequestDto, Long categoryId);

}
