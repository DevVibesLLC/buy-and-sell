package am.devvibes.buyandsell.service.user;

import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;

import java.util.List;

public interface UserService {

	UserResponseDto saveUser(UserRequestDto signUpDto);

	UserResponseDto findUserById(Long id);

	List<UserResponseDto> findAllUsers();

	void deleteUser(Long id);

	UserResponseDto changePassword(String email, String newPassword, String repeatNewPassword);

}
