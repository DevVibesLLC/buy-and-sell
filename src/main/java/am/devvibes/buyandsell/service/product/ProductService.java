package am.devvibes.buyandsell.service.product;

import am.devvibes.buyandsell.model.dto.product.ProductRequestDto;
import am.devvibes.buyandsell.model.dto.product.ProductResponseDto;

import java.util.List;

public interface ProductService {

	ProductResponseDto saveProduct(ProductRequestDto productRequestDto);

	ProductResponseDto findProductById(Long id);

	List<ProductResponseDto> findAllProducts();

	void deleteProduct(Long id);

}
