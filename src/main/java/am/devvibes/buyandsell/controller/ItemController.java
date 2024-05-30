package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemResponseDto;
import am.devvibes.buyandsell.service.item.ItemService;
import am.devvibes.buyandsell.util.page.CustomPageRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/items")
public class ItemController {

	private final ItemService itemService;

	@PostMapping("/{categoryId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Create item")
	public ResponseEntity<ItemResponseDto> createItem(@PathVariable Long categoryId,
			@RequestBody ItemRequestDto itemRequestDto) {
		return ResponseEntity.ok(itemService.save(itemRequestDto, categoryId));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get item by id")
	public ResponseEntity<ItemResponseDto> getItemById(@PathVariable Long id) {
		return ResponseEntity.ok(itemService.findById(id));
	}

	@GetMapping
	@Operation(summary = "Get all items")
	public ResponseEntity<Page<ItemResponseDto>> getAllItem(@RequestParam(required = false) Integer page,
															@RequestParam(required = false) Integer size) {
		PageRequest pageRequest = CustomPageRequest.from(page, size, Sort.unsorted());
		return ResponseEntity.ok(itemService.findAllItems(pageRequest));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Delete item by id")
	public ResponseEntity<List<ItemResponseDto>> deleteItemById(@PathVariable Long id) {
		itemService.deleteById(id);
		return ResponseEntity.ok().build();
	}

}
