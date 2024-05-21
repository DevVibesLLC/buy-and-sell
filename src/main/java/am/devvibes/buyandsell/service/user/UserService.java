package am.devvibes.buyandsell.service.user;

import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;

import java.util.List;

public interface UserService {

	UserResponseDto saveUser(UserRequestDto signUpDto);

	UserResponseDto findUserById(String id);

	List<UserResponseDto> findAllUsers();

	void deleteUser(String id);

	UserResponseDto changePassword(String email, String newPassword, String repeatNewPassword);

}
