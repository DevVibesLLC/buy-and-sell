package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.service.favoriteItems.FavoriteItemsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/favorites")
public class FavoriteItemsController {

	private final FavoriteItemsService favoriteItemsService;
	private final ItemMapper itemMapper;

	@PostMapping("/{userId}/{itemId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Add Item to Favorites")
	public ResponseEntity<List<ItemResponseDto>> addFavoriteItem(@PathVariable String userId,
			@PathVariable Long itemId) {

		List<ItemResponseDto> itemResponseDtos =
				itemMapper.mapEntityListToDtoList(favoriteItemsService.addFavoriteItem(userId, itemId));
		return ResponseEntity.ok(itemResponseDtos);
	}

	@DeleteMapping("/{userId}/{itemId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Delete Users Favorite Item")
	public ResponseEntity<List<ItemResponseDto>> deleteFavoriteItem(@PathVariable String userId,
			@PathVariable Long itemId) {

		List<ItemResponseDto> itemResponseDtos =
				itemMapper.mapEntityListToDtoList(favoriteItemsService.removeFavoriteItem(userId, itemId));
		return ResponseEntity.ok(itemResponseDtos);
	}

}
