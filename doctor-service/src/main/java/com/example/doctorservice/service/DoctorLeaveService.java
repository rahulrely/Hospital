package com.example.doctorservice.service;

import com.example.doctorservice.dto.request.CreateLeaveRequest;
import com.example.doctorservice.dto.response.LeaveResponse;

import java.util.List;

public interface DoctorLeaveService {

    LeaveResponse create(
            Long doctorId,
            CreateLeaveRequest request
    );

    List<LeaveResponse> getLeaves(Long doctorId);
}