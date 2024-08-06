package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.dto.user.UserChangePasswordDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.entity.user.UserEntity;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.mapper.user.UserMapper;
import am.devvibes.buyandsell.service.favoriteItems.FavoriteItemsService;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.service.user.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserServiceImpl userService;
	private final UserMapper userMapper;
	private final FavoriteItemsService favoriteItemsService;
	private final ItemService itemService;
	private final ItemMapper itemMapper;

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Get User by Id")
	public ResponseEntity<UserResponseDto> getUserById(@PathVariable String id) {
		UserResponseDto userResponseDto = userMapper.mapEntityToDto(userService.findUserById(id));
		return ResponseEntity.ok(userResponseDto);
	}

	@GetMapping("/profile")
	@Operation(summary = "Get User by Id for User Profile")
	public ResponseEntity<UserResponseDto> getUserByIdForUserProfile() {
		UserEntity userEntity = userService.findUserForUserProfile();
		return ResponseEntity.ok(userMapper.mapEntityToDto(userEntity));
	}

	@GetMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Get All Users")
	public ResponseEntity<List<UserResponseDto>> getAllUsers() {
		List<UserRepresentation> userRepresentations = userService.findAllUsers();
		return ResponseEntity.ok(userMapper.mapRepresentationListToDtoList(userRepresentations));
	}

	@DeleteMapping("{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Delete User by Id")
	public ResponseEntity<Void> deleteUser(@PathVariable String id) {
		userService.deleteUser(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/items")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Get User Items")
	public ResponseEntity<List<ItemResponseDto>> getUsersItems() {
		List<ItemResponseDto> itemResponseDtos = itemMapper.mapEntityListToDtoList(itemService.findUsersItems());
		return ResponseEntity.ok(itemResponseDtos);
	}

	@GetMapping("/{id}/favorites")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Get Users Favorite Items")
	public ResponseEntity<List<ItemResponseDto>> getUsersFavoriteItems(@PathVariable String id) {
		List<ItemResponseDto> itemResponseDtos =
				itemMapper.mapEntityListToDtoList(favoriteItemsService.getUsersAllFavoriteItems(id));
		return ResponseEntity.ok(itemResponseDtos);
	}

	@PostMapping("/changePassword")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Change Password")
	public ResponseEntity<UserResponseDto> changePassword(@RequestBody UserChangePasswordDto userChangePasswordDto) {
		UserRepresentation userRepresentation =
				userService.changePassword(userChangePasswordDto.getEmail(), userChangePasswordDto.getNewPassword(),
						userChangePasswordDto.getRepeatNewPassword());

		return ResponseEntity.ok(userMapper.mapRepresentationToDto(userRepresentation));
	}

}