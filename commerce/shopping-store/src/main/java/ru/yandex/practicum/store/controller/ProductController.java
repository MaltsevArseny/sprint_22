package ru.yandex.practicum.store.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductDto;
import ru.yandex.practicum.store.service.ProductService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-store")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductDto> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.getProducts(page, size);
    }

    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable UUID productId) {
        return productService.getProduct(productId);
    }

    @PostMapping
    public ProductDto createProduct(@RequestBody @Valid ProductDto dto) {
        return productService.createProduct(dto);
    }

    @PutMapping("/{productId}")
    public ProductDto updateProduct(@PathVariable UUID productId, @RequestBody @Valid ProductDto dto) {
        return productService.updateProduct(productId, dto);
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable UUID productId) {
        productService.deleteProduct(productId);
    }
}
