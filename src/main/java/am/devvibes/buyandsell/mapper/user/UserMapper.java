package am.devvibes.buyandsell.mapper.user;

import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.entity.user.UserEntity;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface UserMapper {

	List<UserResponseDto> mapRepresentationListToDtoList(List<UserRepresentation> userRepresentations);

	UserRepresentation mapDtoToRepresentation(UserRequestDto userRequestDto);

	UserResponseDto mapRepresentationToDto(UserRepresentation userRepresentation);

	UserResponseDto mapEntityToDto(UserEntity user);

}
