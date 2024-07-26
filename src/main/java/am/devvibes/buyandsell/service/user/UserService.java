package am.devvibes.buyandsell.service.user;

import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.entity.user.UserEntity;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface UserService {

	UserRepresentation saveUser(UserRequestDto signUpDto);

	UserEntity findUserById(String id);

	UserEntity findUserForUserProfile();

	List<UserRepresentation> findAllUsers();

	void deleteUser(String id);

	UserRepresentation changePassword(String email, String newPassword, String repeatNewPassword);

}
