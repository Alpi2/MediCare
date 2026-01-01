package com.hospital.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

@Embeddable
public class Address {

  private static final int LENGTH_255 = 255;
  private static final int LENGTH_100 = 100;
  private static final int LENGTH_50 = 50;
  private static final int LENGTH_20 = 20;

  @Column(name = "street_address", length = LENGTH_255)
  @Size(max = LENGTH_255)
  private String streetAddress;

  @Column(name = "apartment_unit", length = LENGTH_50)
  @Size(max = LENGTH_50)
  private String apartmentUnit;

  @Column(name = "city", length = LENGTH_100)
  @Size(max = LENGTH_100)
  private String city;

  @Column(name = "state_province", length = LENGTH_100)
  @Size(max = LENGTH_100)
  private String stateProvince;

  @Column(name = "postal_code", length = LENGTH_20)
  @Size(max = LENGTH_20)
  private String postalCode;

  @Column(name = "country", length = LENGTH_100)
  @Size(max = LENGTH_100)
  private String country = "United States";

  public Address() {
  }

  public Address(
      String streetAddress,
      String apartmentUnit,
      String city,
      String stateProvince,
      String postalCode,
      String country
  ) {
    this.streetAddress = streetAddress;
    this.apartmentUnit = apartmentUnit;
    this.city = city;
    this.stateProvince = stateProvince;
    this.postalCode = postalCode;
    this.country = country;
  }

  public String getStreetAddress() {
    return streetAddress;
  }

  public void setStreetAddress(String streetAddress) {
    this.streetAddress = streetAddress;
  }

  public String getApartmentUnit() {
    return apartmentUnit;
  }

  public void setApartmentUnit(String apartmentUnit) {
    this.apartmentUnit = apartmentUnit;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getStateProvince() {
    return stateProvince;
  }

  public void setStateProvince(String stateProvince) {
    this.stateProvince = stateProvince;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getFullAddress() {
    final StringBuilder sb = new StringBuilder();
    if (streetAddress != null) {
      sb.append(streetAddress);
    }
    if (apartmentUnit != null && !apartmentUnit.isBlank()) {
      sb.append(", ").append(apartmentUnit);
    }
    if (city != null) {
      sb.append(", ").append(city);
    }
    if (stateProvince != null) {
      sb.append(", ").append(stateProvince);
    }
    if (postalCode != null) {
      sb.append(" ").append(postalCode);
    }
    if (country != null) {
      sb.append(", ").append(country);
    }
    return sb.toString();
  }
} 
