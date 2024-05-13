package am.devvibes.buyandsell.mapper;

import am.devvibes.buyandsell.model.dto.UserResponseDto;
import am.devvibes.buyandsell.model.dto.UserSignUpDto;
import am.devvibes.buyandsell.model.entity.UserEntity;

import java.util.List;

public interface UserMapper {

	UserEntity mapDtoToEntity(UserSignUpDto userSignUpDto);

	UserResponseDto mapEntityToDto(UserEntity userEntity);

	List<UserResponseDto> mapEntityListToDtoList(List<UserEntity> userEntities);

}
