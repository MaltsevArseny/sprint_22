package ru.yandex.practicum.store.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductDto;
import ru.yandex.practicum.store.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDto toDto(Product product);

    @Mapping(target = "productId", ignore = true)
    Product toEntity(ProductDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productId", ignore = true)
    void updateFromDto(ProductDto dto, @MappingTarget Product product);
}
