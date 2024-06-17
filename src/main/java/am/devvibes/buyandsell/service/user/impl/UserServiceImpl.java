package am.devvibes.buyandsell.service.user.impl;

import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.entity.user.UserEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.mapper.user.UserMapper;
import am.devvibes.buyandsell.repository.user.UserRepository;
import am.devvibes.buyandsell.service.security.SecurityService;
import am.devvibes.buyandsell.service.user.UserService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final Keycloak keycloak;
	private final SecurityService securityService;

	@Value("${keycloak.realm}")
	private String realm;

	@Override
	@Transactional
	public UserResponseDto saveUser(UserRequestDto signUpDto) {
		validateUser(signUpDto);
		UserRepresentation userRepresentation = userMapper.mapDtoToRepresentation(signUpDto);
		UsersResource usersResource = getUsersResource();
		usersResource.create(userRepresentation);
		return userMapper.mapRepresentationToDto(userRepresentation);
	}

	@Override
	public UserEntity findUserById(String id) {
		return userRepository.findById(id).orElseThrow();
	}

	@Override
	public UserResponseDto findUserForUserProfile() {
		UserEntity userRepresentation =findUserById(securityService.getCurrentUserId());
		return userMapper.toDto(userRepresentation);

	}

	@Override
	@Transactional
	public List<UserResponseDto> findAllUsers() {
		List<UserRepresentation> userRepresentations = getUsersResource().list();
		return userMapper.mapRepresentationListToDtoList(userRepresentations);
	}

	@Override
	@Transactional
	public void deleteUser(String id) {
		getUsersResource().delete(id);
	}

	@Override
	@Transactional
	public UserResponseDto changePassword(String email, String newPassword, String repeatNewPassword) {
		List<UserRepresentation> userRepresentations = getUsersResource().searchByEmail(email, true);
		if (userRepresentations.isEmpty()) {
			throw new NotFoundException(ExceptionConstants.USER_NOT_FOUND);
		}
		comparePasswordsAndValidate(newPassword, repeatNewPassword);
		UserRepresentation userRepresentation = userRepresentations.getFirst();

		CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
		credentialRepresentation.setValue(newPassword);
		credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
		credentialRepresentation.setTemporary(false);

		userRepresentation.setCredentials(List.of(credentialRepresentation));
		return userMapper.mapRepresentationToDto(userRepresentation);
	}

	private UsersResource getUsersResource() {
		RealmResource realm1 = keycloak.realm(realm);
		return realm1.users();
	}

	private void validateUser(UserRequestDto signUpDto) {
		UsersResource usersResource = getUsersResource();
		isEmailUnique(signUpDto.getEmail(), usersResource);
		isUsernameUnique(signUpDto.getUsername(), usersResource);
		comparePasswordsAndValidate(signUpDto.getPassword(), signUpDto.getRepeatPassword());
		EmailValidator.getInstance().isValid(signUpDto.getEmail());
	}

	private void isEmailUnique(String email, UsersResource usersResource) {
		List<UserRepresentation> userRepresentations = usersResource.searchByEmail(email, true);
		if (!userRepresentations.isEmpty()) {
			throw new SomethingWentWrongException(ExceptionConstants.USER_WITH_THAT_EMAIL_ALREADY_EXISTS);
		}
	}

	private void isUsernameUnique(String username, UsersResource usersResource) {
		List<UserRepresentation> userRepresentations = usersResource.searchByUsername(username, true);
		if (!userRepresentations.isEmpty()) {
			throw new SomethingWentWrongException(ExceptionConstants.USER_WITH_THAT_USERNAME_ALREADY_EXISTS);
		}
	}

	private void comparePasswordsAndValidate(String password1, String password2) {
		if (password1.length() < 8 || password2.length() < 8) {
			throw new SomethingWentWrongException(ExceptionConstants.PASSWORD_LENGTH_MUST_BE_MORE_THEN_8);
		}
		if (!Objects.equals(password1, password2)) {
			throw new SomethingWentWrongException(ExceptionConstants.PASSWORDS_ARE_DIFFERENT);
		}
	}

}
