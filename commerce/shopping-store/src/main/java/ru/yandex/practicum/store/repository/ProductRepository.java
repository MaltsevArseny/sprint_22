package ru.yandex.practicum.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.store.model.Product;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
