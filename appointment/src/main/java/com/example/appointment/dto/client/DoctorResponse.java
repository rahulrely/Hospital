package com.example.appointment.dto.client;

import java.math.BigDecimal;

public record DoctorResponse(

        Long id,

        String firstName,

        String lastName,

        String specialization,

        BigDecimal consultationFee,

        Boolean active
) {
}