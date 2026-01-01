package com.hospital.common.dto;

import com.hospital.common.domain.Address;
import jakarta.validation.constraints.Size;

public class AddressDto {
  private static final int STREET_MAX = 255;
  private static final int UNIT_MAX = 50;
  private static final int CITY_MAX = 100;
  private static final int STATE_MAX = 100;
  private static final int POSTAL_MAX = 20;

  @Size(max = STREET_MAX)
  private String streetAddress;

  @Size(max = UNIT_MAX)
  private String apartmentUnit;

  @Size(max = CITY_MAX)
  private String city;

  @Size(max = STATE_MAX)
  private String stateProvince;

  @Size(max = POSTAL_MAX)
  private String postalCode;

  private String country;

  public AddressDto() {
  }

  public AddressDto(
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

  public Address toEntity() {
    final Address a = new Address();
    a.setStreetAddress(this.streetAddress);
    a.setApartmentUnit(this.apartmentUnit);
    a.setCity(this.city);
    a.setStateProvince(this.stateProvince);
    a.setPostalCode(this.postalCode);
    a.setCountry(this.country);
    return a;
  }

  public static AddressDto fromEntity(final Address a) {
    if (a == null) {
      return null;
    }
    final AddressDto dto = new AddressDto();
    dto.setStreetAddress(a.getStreetAddress());
    dto.setApartmentUnit(a.getApartmentUnit());
    dto.setCity(a.getCity());
    dto.setStateProvince(a.getStateProvince());
    dto.setPostalCode(a.getPostalCode());
    dto.setCountry(a.getCountry());
    return dto;
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
}
