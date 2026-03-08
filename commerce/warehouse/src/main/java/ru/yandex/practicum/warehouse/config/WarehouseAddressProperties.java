package ru.yandex.practicum.warehouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "warehouse.address")
public class WarehouseAddressProperties {

    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getHouse() { return house; }
    public void setHouse(String house) { this.house = house; }

    public String getFlat() { return flat; }
    public void setFlat(String flat) { this.flat = flat; }
}
