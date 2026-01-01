package com.hospital.patient.mapper;

import com.hospital.patient.domain.Patient;
import com.hospital.patient.dto.PatientCreateRequest;
import com.hospital.patient.dto.PatientResponse;
import com.hospital.patient.dto.PatientUpdateRequest;
import com.hospital.common.dto.AddressDto;
import com.hospital.common.domain.Address;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PatientMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "medicalRecordNumber", ignore = true)
  @Mapping(target = "status", constant = "ACTIVE")
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  @Mapping(target = "medicalRecords", ignore = true)
  @Mapping(target = "vitalSigns", ignore = true)
  Patient toEntity(PatientCreateRequest request);

  @Mapping(target = "fullName", expression = "java(patient.getFullName())")
  @Mapping(target = "age", expression = "java(patient.getAge())")
  @Mapping(target = "hasAllergies", expression = "java(patient.hasAllergies())")
  @Mapping(target = "hasMedicalConditions", expression = "java(patient.hasMedicalConditions())")
  @Mapping(target = "isActive", expression = "java(patient.isActive())")
  PatientResponse toResponse(Patient patient);

  void updateEntityFromRequest(PatientUpdateRequest request, @MappingTarget Patient patient);

  List<PatientResponse> toResponseList(List<Patient> patients);

  default Page<PatientResponse> toResponsePage(Page<Patient> patients) {
    List<PatientResponse> content = toResponseList(patients.getContent());
    return new PageImpl<>(content, patients.getPageable(), patients.getTotalElements());
  }

  default Address mapAddressDto(AddressDto dto) {
    if (dto == null) {
      return null;
    }
    Address a = new Address();
    a.setStreetAddress(dto.getStreetAddress());
    a.setApartmentUnit(dto.getApartmentUnit());
    a.setCity(dto.getCity());
    a.setStateProvince(dto.getStateProvince());
    a.setPostalCode(dto.getPostalCode());
    a.setCountry(dto.getCountry());
    return a;
  }

  default AddressDto mapAddress(Address address) {
    if (address == null) {
      return null;
    }
    AddressDto dto = new AddressDto();
    dto.setStreetAddress(address.getStreetAddress());
    dto.setApartmentUnit(address.getApartmentUnit());
    dto.setCity(address.getCity());
    dto.setStateProvince(address.getStateProvince());
    dto.setPostalCode(address.getPostalCode());
    dto.setCountry(address.getCountry());
    return dto;
  }
}
