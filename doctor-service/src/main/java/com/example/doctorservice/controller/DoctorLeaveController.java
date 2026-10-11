package com.example.doctorservice.controller;


import com.example.doctorservice.dto.request.CreateLeaveRequest;
import com.example.doctorservice.dto.response.LeaveResponse;
import com.example.doctorservice.service.DoctorLeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/doctors/{doctorId}/leaves")
@RequiredArgsConstructor
public class DoctorLeaveController {

    private final DoctorLeaveService leaveService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveResponse create(
            @PathVariable Long doctorId,
            @Valid @RequestBody CreateLeaveRequest request
    ) {

        return leaveService.create(
                doctorId,
                request
        );
    }

    @GetMapping
    public List<LeaveResponse> getLeaves(
            @PathVariable Long doctorId
    ) {

        return leaveService.getLeaves(doctorId);
    }
}