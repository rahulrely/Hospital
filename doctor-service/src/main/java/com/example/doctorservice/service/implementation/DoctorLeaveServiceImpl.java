package com.example.doctorservice.service.implementation;


import com.example.doctorservice.entity.Doctor;
import com.example.doctorservice.entity.DoctorLeave;
import com.example.doctorservice.exception.ResourceNotFoundException;
import com.example.doctorservice.repository.DoctorLeaveRepository;
import com.example.doctorservice.repository.DoctorRepository;
import com.example.doctorservice.dto.request.CreateLeaveRequest;
import com.example.doctorservice.dto.response.LeaveResponse;
import com.example.doctorservice.service.DoctorLeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorLeaveServiceImpl
        implements DoctorLeaveService {

    private final DoctorRepository doctorRepository;
    private final DoctorLeaveRepository leaveRepository;

    @Override
    public LeaveResponse create(
            Long doctorId,
            CreateLeaveRequest request
    ) {

        if (request.startDate()
                .isAfter(request.endDate())) {

            throw new IllegalArgumentException(
                    "Start date must be before end date"
            );
        }

        Doctor doctor =
                doctorRepository.findById(doctorId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Doctor",
                                        doctorId
                                ));

        DoctorLeave leave =
                DoctorLeave.builder()
                        .doctor(doctor)
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .reason(request.reason())
                        .approved(true)
                        .build();

        return toResponse(
                leaveRepository.save(leave)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeaves(
            Long doctorId
    ) {

        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException(
                    "Doctor",
                    doctorId
            );
        }

        return leaveRepository
                .findByDoctorIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        doctorId,
                        java.time.LocalDate.of(9999, 12, 31),
                        java.time.LocalDate.of(1, 1, 1)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LeaveResponse toResponse(
            DoctorLeave leave
    ) {

        return new LeaveResponse(
                leave.getId(),
                leave.getDoctor().getId(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getApproved()
        );
    }
}