package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.itemForSell.ItemForSellRequestDto;
import am.devvibes.buyandsell.dto.itemForSell.ItemForSellResponseDto;
import am.devvibes.buyandsell.service.itemForSell.ItemForSellService;
import am.devvibes.buyandsell.service.itemForSell.impl.ItemForSellServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/itemForSell")
public class ItemForSellController {

	private final ItemForSellService itemForSell;

	@PostMapping
	public ResponseEntity<ItemForSellResponseDto> registerUser(@RequestBody @Valid ItemForSellRequestDto itemForSellRequestDto) {
		ItemForSellResponseDto savedItemForSell = itemForSell.saveItemForSell(itemForSellRequestDto);
		return ResponseEntity.ok(savedItemForSell);
	}

	@GetMapping("{id}")
	public ResponseEntity<ItemForSellResponseDto> getItemForSellById(@PathVariable @Positive Long id) {
		ItemForSellResponseDto itemForSellById = itemForSell.findItemForSellById(id);
		return ResponseEntity.ok(itemForSellById);
	}

	@GetMapping
	public ResponseEntity<List<ItemForSellResponseDto>> getAllItemForSells() {
		List<ItemForSellResponseDto> allItemForSells = itemForSell.findAllItemForSells();
		return ResponseEntity.ok(allItemForSells);
	}

	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteItemForSell(@PathVariable @Positive Long id) {
		itemForSell.deleteItemForSell(id);
		return ResponseEntity.ok().build();
	}

}