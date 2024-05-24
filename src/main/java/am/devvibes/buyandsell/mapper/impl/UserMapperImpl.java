package am.devvibes.buyandsell.mapper.impl;

import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.entity.UserEntity;
import am.devvibes.buyandsell.mapper.UserMapper;
import am.devvibes.buyandsell.repository.RoleRepository;
import am.devvibes.buyandsell.util.Role;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {

	@Override
	public List<UserResponseDto> mapRepresentationListToDtoList(List<UserRepresentation> representations) {
		List<UserResponseDto> userResponseDtoList = new ArrayList<>();
		for (UserRepresentation userRepresentation : representations) {
			UserResponseDto userResponseDto = mapRepresentationToDto(userRepresentation);
			userResponseDtoList.add(userResponseDto);
		}
		return userResponseDtoList;
	}

	@Override
	public UserRepresentation mapDtoToRepresentation(UserRequestDto userRequestDto) {
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setEnabled(true);
		userRepresentation.setUsername(userRequestDto.getUsername());
		userRepresentation.setEmail(userRequestDto.getEmail());
		userRepresentation.setFirstName(userRequestDto.getFirstName());
		userRepresentation.setLastName(userRequestDto.getLastName());
		userRepresentation.setCreatedTimestamp(System.currentTimeMillis());
		userRepresentation.setEmailVerified(false);

		CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
		credentialRepresentation.setValue(userRequestDto.getPassword());
		credentialRepresentation.setTemporary(false);
		credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

		List<CredentialRepresentation> list = new ArrayList<>();
		list.add(credentialRepresentation);
		userRepresentation.setCredentials(list);

		return userRepresentation;
	}

	@Override
	public UserResponseDto mapRepresentationToDto(UserRepresentation userRepresentation) {
		return UserResponseDto.builder()
				.id(userRepresentation.getId())
				.username(userRepresentation.getUsername())
				.firstName(userRepresentation.getFirstName())
				.lastName(userRepresentation.getLastName())
				.email(userRepresentation.getEmail())
				.createdAt(getTimeFromMillis(userRepresentation.getCreatedTimestamp()))
				.isEnabled(userRepresentation.isEnabled())
				.isVerified(false)
				.isAccountNonLocked(true)
				.isAccountNonExpired(true)
				.isCredentialsNonExpired(true)
				.build();
	}

	@Override
	public UserResponseDto toDto(UserEntity user) {
		return UserResponseDto.builder()
				.id(user.getId())
				.username(user.getUsername())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.email(user.getEmail())
				.createdAt(getTimeFromMillis(user.getCreatedTimestamp()))
				.isEnabled(user.isEnabled())
				.isVerified(user.isEmailVerified())
				.build();
	}

	private LocalDateTime getTimeFromMillis(Long stamp) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(stamp), ZoneId.systemDefault());
	}

}
