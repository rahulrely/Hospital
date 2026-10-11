package com.example.patientservice.mapper;

import com.example.patientservice.dto.SignUpRequest;
import com.example.patientservice.dto.PatientProfileResponse;
import com.example.patientservice.entity.Patient;
import com.example.patientservice.dto.PatientDto;
import com.example.patientservice.Security.JwtUserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    Patient toEntity(SignUpRequest signupRequest);

    @Mapping(source = "patientId", target = "id")
    @Mapping(source = "firstname", target = "firstName")
    @Mapping(source = "lastname", target = "lastName")
    PatientProfileResponse toPatientProfileResponse(JwtUserPrincipal patient);

    PatientDto toPatientDto(Patient patient);
}