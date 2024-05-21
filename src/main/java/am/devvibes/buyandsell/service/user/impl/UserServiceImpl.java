package am.devvibes.buyandsell.service.user.impl;

import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.entity.UserEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.mapper.UserMapper;
import am.devvibes.buyandsell.repository.UserRepository;
import am.devvibes.buyandsell.service.user.UserService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import am.devvibes.buyandsell.util.RandomGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
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
	@Transactional
	public UserResponseDto findUserById(String id) {
		UserRepresentation userRepresentation = getUsersResource().get(id).toRepresentation();
		return userMapper.mapRepresentationToDto(userRepresentation);
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

	private RolesResource getRoleResource() {
		RealmResource realm1 = keycloak.realm(realm);
		return realm1.roles();
	}

	private UserEntity setVerificationCodeAndSendMail(UserEntity userEntity) {
		userEntity.setVerificationCode(RandomGenerator.generateNumericString());
		/*emailService.sendMessage(userEntity.getEmail(), "Verification",
				"Your verification code is: " + userEntity.getVerificationCode());*/
		return userRepository.save(userEntity);
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
			throw new SomethingWentWrongException(ExceptionConstants.USER_WITH_THIS_EMAIL_ALREADY_EXISTS);
		}
	}

	private void isUsernameUnique(String username, UsersResource usersResource) {
		List<UserRepresentation> userRepresentations = usersResource.searchByUsername(username, true);
		if (!userRepresentations.isEmpty()) {
			throw new SomethingWentWrongException(ExceptionConstants.USER_WITH_THIS_USERNAME_ALREADY_EXISTS);
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
