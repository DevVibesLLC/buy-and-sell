package am.devvibes.buyandsell.service;

import am.devvibes.buyandsell.model.dto.UserResponseDto;
import am.devvibes.buyandsell.model.dto.UserSignUpDto;

import java.util.List;

public interface UserService {

	UserResponseDto saveUser(UserSignUpDto signUpDto);

	UserResponseDto findUserById(Long id);

	List<UserResponseDto> findAllUsers();

	void deleteUser(Long id);

	UserResponseDto changePassword(String email, String oldPassword, String newPassword, String repeatNewPassword);

}
