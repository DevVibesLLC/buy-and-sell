package am.devvibes.buyandsell.service.user;

import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.entity.user.UserEntity;

import java.util.List;

public interface UserService {

	UserResponseDto saveUser(UserRequestDto signUpDto);

	UserEntity findUserById(String id);

	UserResponseDto findUserForUserProfile();

	List<UserResponseDto> findAllUsers();

	void deleteUser(String id);

	UserResponseDto changePassword(String email, String newPassword, String repeatNewPassword);

}
