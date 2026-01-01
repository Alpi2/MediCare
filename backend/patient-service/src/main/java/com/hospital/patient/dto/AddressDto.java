package com.hospital.patient.dto;

/**
 * Deprecated compatibility shim.
 *
 * The canonical `AddressDto` type lives in `com.hospital.common.dto.AddressDto`.
 * Import and use that type directly. This shim exists to avoid breaking
 * older code but will be removed in a future release.
 */
@Deprecated(forRemoval = true)
public final class AddressDto extends com.hospital.common.dto.AddressDto {

  @Deprecated(forRemoval = true)
  public AddressDto() {
    super();
  }

  @Deprecated(forRemoval = true)
  public AddressDto(com.hospital.common.dto.AddressDto other) {
    super(other.getStreetAddress(), other.getApartmentUnit(), other.getCity(), other.getStateProvince(), other.getPostalCode(), other.getCountry());
  }

  @Deprecated(forRemoval = true)
  public static AddressDto fromCommon(com.hospital.common.dto.AddressDto common) {
    return common == null ? null : new AddressDto(common);
  }

  @Deprecated(forRemoval = true)
  public com.hospital.common.dto.AddressDto toCommon() {
    com.hospital.common.dto.AddressDto c = new com.hospital.common.dto.AddressDto();
    c.setStreetAddress(this.getStreetAddress());
    c.setApartmentUnit(this.getApartmentUnit());
    c.setCity(this.getCity());
    c.setStateProvince(this.getStateProvince());
    c.setPostalCode(this.getPostalCode());
    c.setCountry(this.getCountry());
    return c;
  }
}
