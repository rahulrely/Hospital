package com.example.doctorservice.repository;

import com.example.doctorservice.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorScheduleRepository
        extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByDoctorIdAndDayOfWeekAndActiveTrue(
            Long doctorId,
            DayOfWeek dayOfWeek
    );

    List<DoctorSchedule> findByDoctorIdAndActiveTrue(
            Long doctorId
    );
}