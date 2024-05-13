package am.devvibes.buyandsell.mapper.impl;

import am.devvibes.buyandsell.mapper.UserMapper;
import am.devvibes.buyandsell.model.dto.UserResponseDto;
import am.devvibes.buyandsell.model.dto.UserSignUpDto;
import am.devvibes.buyandsell.model.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {

	private final PasswordEncoder passwordEncoder;

	@Override
	public UserEntity mapDtoToEntity(UserSignUpDto userSignUpDto) {
		return UserEntity.builder()
				.email(userSignUpDto.getEmail())
				.password(passwordEncoder.encode(userSignUpDto.getPassword()))
				.name(userSignUpDto.getName())
				.secondName(userSignUpDto.getSecondName())
				.isAccountNonLocked(true)
				.isAccountNonExpired(true)
				.isCredentialsNonExpired(true)
				.isEnabled(true)
				.build();
	}

	@Override
	public UserResponseDto mapEntityToDto(UserEntity userEntity) {
		return UserResponseDto.builder()
				.id(userEntity.getId())
				.email(userEntity.getEmail())
				.name(userEntity.getName())
				.secondName(userEntity.getSecondName())
				.isAccountNonLocked(userEntity.getIsAccountNonLocked())
				.isAccountNonExpired(userEntity.getIsAccountNonExpired())
				.isCredentialsNonExpired(userEntity.getIsCredentialsNonExpired())
				.isEnabled(userEntity.getIsEnabled())
				.createdAt(userEntity.getCreatedAt())
				.updatedAt(userEntity.getUpdatedAt())
				.build();
	}

	@Override
	public List<UserResponseDto> mapEntityListToDtoList(List<UserEntity> userEntities) {
		List<UserResponseDto> userResponseDtoList = new ArrayList<>();
		for (UserEntity userEntity : userEntities) {
			UserResponseDto userResponseDto = mapEntityToDto(userEntity);
			userResponseDtoList.add(userResponseDto);
		}
		return userResponseDtoList;
	}

}
