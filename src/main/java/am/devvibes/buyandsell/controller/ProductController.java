package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.model.dto.product.ProductRequestDto;
import am.devvibes.buyandsell.model.dto.product.ProductResponseDto;
import am.devvibes.buyandsell.service.product.impl.ProductServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

	private final ProductServiceImpl productService;

	@PostMapping
	public ResponseEntity<ProductResponseDto> registerUser(@RequestBody @Valid ProductRequestDto productRequestDto) {
		ProductResponseDto savedProduct = productService.saveProduct(productRequestDto);
		return ResponseEntity.ok(savedProduct);
	}

	@GetMapping("{id}")
	public ResponseEntity<ProductResponseDto> getProductById(@PathVariable @Positive Long id) {
		ProductResponseDto productById = productService.findProductById(id);
		return ResponseEntity.ok(productById);
	}

	@GetMapping
	public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
		List<ProductResponseDto> allProducts = productService.findAllProducts();
		return ResponseEntity.ok(allProducts);
	}

	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable @Positive Long id) {
		productService.deleteProduct(id);
		return ResponseEntity.ok().build();
	}

}