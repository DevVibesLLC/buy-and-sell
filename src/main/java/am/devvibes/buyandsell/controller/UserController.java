package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.user.UserChangePasswordDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.mapper.user.UserMapper;
import am.devvibes.buyandsell.service.favoriteItems.FavoriteItemsService;
import am.devvibes.buyandsell.service.user.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserServiceImpl userService;
	private final UserMapper userMapper;
	private final FavoriteItemsService favoriteItemsService;
	private final ItemMapper itemMapper;

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<UserResponseDto> getUserById(@PathVariable String id) {
		UserResponseDto userResponseDto = userMapper.toDto(userService.findUserById(id));
		return ResponseEntity.ok(userResponseDto);
	}

	@GetMapping("/profile")
	public ResponseEntity<UserResponseDto> getUserByIdForUserProfile() {
		UserResponseDto userResponseDto = userService.findUserForUserProfile();
		return ResponseEntity.ok(userResponseDto);
	}

	@GetMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<UserResponseDto>> getAllUsers() {
		List<UserResponseDto> allUsers = userService.findAllUsers();
		return ResponseEntity.ok(allUsers);
	}

	@DeleteMapping("{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<Void> deleteUser(@PathVariable String id) {
		userService.deleteUser(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{id}/favorites")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<List<ItemResponseDto>> getUsersFavoriteItems(@PathVariable String id) {
		List<ItemResponseDto> itemResponseDtos =
				itemMapper.mapEntityListToDtoList(favoriteItemsService.getUsersAllFavoriteItems(id));
		return ResponseEntity.ok(itemResponseDtos);
	}

	@PostMapping("/changePassword")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<UserResponseDto> changePassword(@RequestBody UserChangePasswordDto userChangePasswordDto) {
		UserResponseDto userResponseDto =
				userService.changePassword(
						userChangePasswordDto.getEmail(),
						userChangePasswordDto.getNewPassword(),
						userChangePasswordDto.getRepeatNewPassword());

		return ResponseEntity.ok(userResponseDto);
	}

}