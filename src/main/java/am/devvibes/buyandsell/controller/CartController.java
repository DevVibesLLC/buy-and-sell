package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.mapper.item.ItemMapper;
import am.devvibes.buyandsell.service.cart.CartService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

	private final CartService cartService;
	private final ItemMapper itemMapper;

	@PostMapping("/{itemId}")
	@Operation(summary = "Add Item to Cart")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<List<ItemResponseDto>> addItemToCart(@PathVariable Long itemId) {
		List<ItemEntity> itemEntities = cartService.addItemToCart(itemId);
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemEntities));
	}

	@DeleteMapping("/{itemId}")
	@Operation(summary = "Delete Item from Cart")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<List<ItemResponseDto>> removeItemFromCart(@PathVariable Long itemId) {
		List<ItemEntity> itemEntities = cartService.removeItemFromCart(itemId);
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemEntities));
	}

	@GetMapping
	@Operation(summary = "Get User Cart")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<List<ItemResponseDto>> getUserCart() {
		List<ItemEntity> itemEntities = cartService.getUsersCart();
		return ResponseEntity.ok(itemMapper.mapEntityListToDtoList(itemEntities));
	}

}
