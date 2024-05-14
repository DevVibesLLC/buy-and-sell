package am.devvibes.buyandsell.service.user;

import am.devvibes.buyandsell.model.dto.user.UserResponseDto;
import am.devvibes.buyandsell.model.dto.user.UserRequestDto;

import java.util.List;

public interface UserService {

	UserResponseDto saveUser(UserRequestDto signUpDto);

	UserResponseDto findUserById(Long id);

	List<UserResponseDto> findAllUsers();

	void deleteUser(Long id);

	UserResponseDto changePassword(String email, String oldPassword, String newPassword, String repeatNewPassword);

}
