package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.service.itemForSell.ItemForSellService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/itemforsell")
public class ItemForSellController {

	private final ItemForSellService itemForSell;

	@PostMapping
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<ItemForSellResponseDto> saveItem(
			@RequestBody @Valid ItemForSellRequestDto itemForSellRequestDto) {
		ItemForSellResponseDto savedItemForSell = itemForSell.saveItemForSell(itemForSellRequestDto);
		var auth = SecurityContextHolder.getContext().getAuthentication();
		return ResponseEntity.ok(savedItemForSell);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ItemForSellResponseDto> getItemForSellById(@PathVariable @Positive Long id) {
		ItemForSellResponseDto itemForSellById = itemForSell.findItemForSellById(id);
		return ResponseEntity.ok(itemForSellById);
	}

	@GetMapping
	public ResponseEntity<List<ItemForSellResponseDto>> getAllItemForSells() {
		List<ItemForSellResponseDto> allItemForSells = itemForSell.findAllItemForSells();
		return ResponseEntity.ok(allItemForSells);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<Void> deleteItemForSell(@PathVariable @Positive Long id) {
		itemForSell.deleteItemForSell(id);
		return ResponseEntity.ok().build();
	}

}