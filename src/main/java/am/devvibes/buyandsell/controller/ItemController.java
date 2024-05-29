package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.service.item.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/items")
public class ItemController {

	private final ItemService itemService;

	@PostMapping("/{categoryId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<ItemResponseDto> createItem(@PathVariable Long categoryId,
			@RequestBody ItemRequestDto itemRequestDto) {
		return ResponseEntity.ok(itemService.save(itemRequestDto, categoryId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ItemResponseDto> getItemById(@PathVariable Long id) {
		return ResponseEntity.ok(itemService.findById(id));
	}

	@GetMapping
	public ResponseEntity<List<ItemResponseDto>> getAllItem() {
		return ResponseEntity.ok(itemService.findAllItems());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<List<ItemResponseDto>> deleteItemById(@PathVariable Long id) {
		itemService.deleteById(id);
		return ResponseEntity.ok().build();
	}

}
