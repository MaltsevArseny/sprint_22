package ru.yandex.practicum.store.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductDto;
import ru.yandex.practicum.store.mapper.ProductMapper;
import ru.yandex.practicum.store.model.Product;
import ru.yandex.practicum.store.repository.ProductRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size))
                .map(productMapper::toDto)
                .getContent();
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID productId) {
        return productMapper.toDto(findProduct(productId));
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = productMapper.toEntity(dto);
        return productMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto updateProduct(UUID productId, ProductDto dto) {
        Product product = findProduct(productId);
        productMapper.updateFromDto(dto, product);
        return productMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new NoSuchElementException("Product not found: " + productId);
        }
        productRepository.deleteById(productId);
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));
    }
}
