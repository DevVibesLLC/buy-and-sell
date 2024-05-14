package am.devvibes.buyandsell.mapper;

import am.devvibes.buyandsell.model.dto.user.UserResponseDto;
import am.devvibes.buyandsell.model.dto.user.UserRequestDto;
import am.devvibes.buyandsell.model.entity.UserEntity;

import java.util.List;

public interface UserMapper {

	UserEntity mapDtoToEntity(UserRequestDto userSignUpDto);

	UserResponseDto mapEntityToDto(UserEntity userEntity);

	List<UserResponseDto> mapEntityListToDtoList(List<UserEntity> userEntities);

}
