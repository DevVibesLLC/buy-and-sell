package am.devvibes.buyandsell.mapper;

import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.entity.UserEntity;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface UserMapper {

	//UserEntity mapDtoToEntity(UserRequestDto userSignUpDto);

	//UserResponseDto mapEntityToDto(UserEntity userEntity);

	List<UserResponseDto> mapRepresentationListToDtoList(List<UserRepresentation> userRepresentations);

	UserRepresentation mapDtoToRepresentation(UserRequestDto userRequestDto);

	UserResponseDto mapRepresentationToDto(UserRepresentation userRepresentation);

	UserResponseDto toDto(UserEntity user);

}
