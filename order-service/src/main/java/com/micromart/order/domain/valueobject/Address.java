package com.micromart.order.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Address Value Object - Shipping/Billing address.
 * <p>
 * PEAA Pattern: Value Object
 * Immutable object that represents an address.
 * <p>
 * JPA Annotation: @Embeddable
 * Can be embedded in Order entity for shipping/billing addresses.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    private String country;

    /**
     * Factory method for creating an Address.
     */
    public static Address of(String street, String city, String state,
                             String postalCode, String country) {
        return Address.builder()
                .street(street)
                .city(city)
                .state(state)
                .postalCode(postalCode)
                .country(country)
                .build();
    }

    /**
     * Get formatted single-line address.
     */
    public String getFormattedAddress() {
        return String.format("%s, %s, %s %s, %s",
                street, city, state, postalCode, country);
    }

    @Override
    public String toString() {
        return getFormattedAddress();
    }
}
