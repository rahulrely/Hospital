package com.example.doctorservice.controller;

import com.example.doctorservice.dto.request.CreateDoctorRequest;
import com.example.doctorservice.dto.request.UpdateDoctorRequest;
import com.example.doctorservice.dto.response.DoctorResponse;
import com.example.doctorservice.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse create(
            @Valid @RequestBody CreateDoctorRequest request
    ) {

        return doctorService.create(request);
    }

    @GetMapping("/{id}")
    public DoctorResponse getById(
            @PathVariable Long id
    ) {

        return doctorService.getById(id);
    }

    @GetMapping
    public Page<DoctorResponse> getAll(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        return doctorService.getAll(pageable);
    }

    @GetMapping("/search")
    public List<DoctorResponse> search(
            @RequestParam String specialization
    ) {

        return doctorService
                .getBySpecialization(specialization);
    }

    @PutMapping("/{id}")
    public DoctorResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDoctorRequest request
    ) {

        return doctorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {

        doctorService.delete(id);
    }
}