package ru.yandex.practicum.delivery.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;

    public Address() {}

    public Address(String country, String city, String street, String house, String flat) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.house = house;
        this.flat = flat;
    }

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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String country;
        private String city;
        private String street;
        private String house;
        private String flat;

        public Builder country(String country) { this.country = country; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder street(String street) { this.street = street; return this; }
        public Builder house(String house) { this.house = house; return this; }
        public Builder flat(String flat) { this.flat = flat; return this; }

        public Address build() {
            return new Address(country, city, street, house, flat);
        }
    }

    @Override
    public String toString() {
        return "Address{country='" + country + "', city='" + city + "', street='" + street
                + "', house='" + house + "', flat='" + flat + "'}";
    }
}
