package am.devvibes.buyandsell.mapper.impl;

import am.devvibes.buyandsell.mapper.UserMapper;
import am.devvibes.buyandsell.model.dto.user.UserResponseDto;
import am.devvibes.buyandsell.model.dto.user.UserRequestDto;
import am.devvibes.buyandsell.model.entity.UserEntity;
import am.devvibes.buyandsell.repository.RoleRepository;
import am.devvibes.buyandsell.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {

	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;

	@Override
	public UserEntity mapDtoToEntity(UserRequestDto userSignUpDto) {
		return UserEntity.builder()
				.email(userSignUpDto.getEmail())
				.password(passwordEncoder.encode(userSignUpDto.getPassword()))
				.name(userSignUpDto.getName())
				.secondName(userSignUpDto.getSecondName())
				.isVerified(false)
				.isAccountNonLocked(true)
				.isAccountNonExpired(true)
				.isCredentialsNonExpired(true)
				.isEnabled(true)
				.roleEntity(roleRepository.findByRole(Role.ROLE_USER))
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
