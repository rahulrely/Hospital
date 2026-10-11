package com.example.doctorservice.repository;


import com.example.doctorservice.entity.DoctorLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DoctorLeaveRepository
        extends JpaRepository<DoctorLeave, Long> {

    List<DoctorLeave> findByDoctorIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long doctorId,
            LocalDate date1,
            LocalDate date2
    );
}