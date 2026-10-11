package com.example.doctorservice.dto.response;

import java.math.BigDecimal;

public record DoctorResponse(

        Long id,

        String firstName,

        String lastName,

        String email,

        String phone,

        String specialization,

        String qualification,

        Integer experienceYears,

        BigDecimal consultationFee,

        Boolean active
) {
}