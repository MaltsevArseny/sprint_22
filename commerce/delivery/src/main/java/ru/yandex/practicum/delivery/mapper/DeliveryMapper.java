package ru.yandex.practicum.delivery.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.delivery.model.Address;
import ru.yandex.practicum.delivery.model.Delivery;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {

    DeliveryDto toDto(Delivery delivery);

    Delivery toEntity(DeliveryDto dto);

    Address toAddress(AddressDto dto);

    AddressDto toAddressDto(Address address);
}
