package am.devvibes.buyandsell.service.user;

import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;

import java.util.List;

public interface UserService {

	UserResponseDto saveUser(UserRequestDto signUpDto);

	void emailVerification(String userId);

	UserResponseDto findUserById(String id);

	List<UserResponseDto> findAllUsers();

	void deleteUser(String id);

	UserResponseDto changePassword(String email, String newPassword, String repeatNewPassword);

}
