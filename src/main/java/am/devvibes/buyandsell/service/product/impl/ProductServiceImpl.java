package am.devvibes.buyandsell.service.product.impl;

import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.mapper.ProductMapper;
import am.devvibes.buyandsell.model.dto.product.ProductRequestDto;
import am.devvibes.buyandsell.model.dto.product.ProductResponseDto;
import am.devvibes.buyandsell.model.entity.ItemForSell;
import am.devvibes.buyandsell.repository.ProductRepository;
import am.devvibes.buyandsell.service.product.ProductService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ProductMapper productMapper;

	@Override
	@Transactional
	public ProductResponseDto saveProduct(ProductRequestDto productRequestDto) {
		ItemForSell productEntity = productMapper.mapDtoToEntity(productRequestDto);
		ItemForSell savedProduct = productRepository.save(productEntity);
		return productMapper.mapEntityToDto(savedProduct);
	}

	@Override
	@Transactional
	public ProductResponseDto findProductById(Long id) {
		ItemForSell productEntity = productRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.PRODUCT_NOT_FOUND));
		return productMapper.mapEntityToDto(productEntity);
	}

	@Override
	@Transactional
	public List<ProductResponseDto> findAllProducts() {
		List<ItemForSell> productEntityList = productRepository.findAll();
		return productMapper.mapEntityListToDtoList(productEntityList);
	}

	@Override
	@Transactional
	public void deleteProduct(Long id) {
		productRepository.deleteById(id);
	}

}
