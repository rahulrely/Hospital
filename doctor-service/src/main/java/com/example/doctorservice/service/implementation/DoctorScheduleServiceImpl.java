package com.example.doctorservice.service.implementation;

import com.example.doctorservice.dto.request.CreateScheduleRequest;
import com.example.doctorservice.dto.response.DoctorAvailabilityResponse;
import com.example.doctorservice.dto.response.ScheduleResponse;
import com.example.doctorservice.entity.Doctor;
import com.example.doctorservice.entity.DoctorSchedule;
import com.example.doctorservice.exception.ResourceNotFoundException;
import com.example.doctorservice.repository.DoctorLeaveRepository;
import com.example.doctorservice.repository.DoctorRepository;
import com.example.doctorservice.repository.DoctorScheduleRepository;
import com.example.doctorservice.service.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleServiceImpl
        implements DoctorScheduleService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorLeaveRepository leaveRepository;

    @Override
    public ScheduleResponse create(
            Long doctorId,
            CreateScheduleRequest request
    ) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor",
                                doctorId
                        ));

        validateTime(
                request.startTime(),
                request.endTime()
        );

        DoctorSchedule schedule =
                DoctorSchedule.builder()
                        .doctor(doctor)
                        .dayOfWeek(request.dayOfWeek())
                        .startTime(request.startTime())
                        .endTime(request.endTime())
                        .slotDurationMinutes(
                                request.slotDurationMinutes()
                        )
                        .active(true)
                        .build();

        return toResponse(
                scheduleRepository.save(schedule)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getDoctorSchedules(
            Long doctorId
    ) {

        verifyDoctor(doctorId);

        return scheduleRepository
                .findByDoctorIdAndActiveTrue(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ScheduleResponse update(
            Long doctorId,
            Long scheduleId,
            CreateScheduleRequest request
    ) {

        DoctorSchedule schedule =
                scheduleRepository.findById(scheduleId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Schedule",
                                        scheduleId
                                ));

        if (!schedule.getDoctor().getId().equals(doctorId)) {
            throw new IllegalArgumentException(
                    "Schedule does not belong to doctor"
            );
        }

        validateTime(
                request.startTime(),
                request.endTime()
        );

        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setSlotDurationMinutes(
                request.slotDurationMinutes()
        );

        return toResponse(schedule);
    }

    @Override
    public void delete(
            Long doctorId,
            Long scheduleId
    ) {

        DoctorSchedule schedule =
                scheduleRepository.findById(scheduleId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Schedule",
                                        scheduleId
                                ));

        if (!schedule.getDoctor().getId().equals(doctorId)) {
            throw new IllegalArgumentException(
                    "Schedule does not belong to doctor"
            );
        }

        schedule.setActive(false);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorAvailabilityResponse getAvailability(
            Long doctorId,
            LocalDate date
    ) {

        verifyDoctor(doctorId);

        boolean onLeave =
                !leaveRepository
                        .findByDoctorIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                doctorId,
                                date,
                                date
                        )
                        .isEmpty();

        if (onLeave) {

            return new DoctorAvailabilityResponse(
                    doctorId,
                    date,
                    false,
                    List.of()
            );
        }

        List<DoctorSchedule> schedules =
                scheduleRepository
                        .findByDoctorIdAndDayOfWeekAndActiveTrue(
                                doctorId,
                                date.getDayOfWeek()
                        );

        List<LocalTime> slots = new ArrayList<>();

        for (DoctorSchedule schedule : schedules) {

            LocalTime current =
                    schedule.getStartTime();

            while (
                    !current.plusMinutes(
                            schedule.getSlotDurationMinutes()
                    ).isAfter(schedule.getEndTime())
            ) {

                slots.add(current);

                current = current.plusMinutes(
                        schedule.getSlotDurationMinutes()
                );
            }
        }

        return new DoctorAvailabilityResponse(
                doctorId,
                date,
                !slots.isEmpty(),
                slots
        );
    }

    private void verifyDoctor(Long doctorId) {

        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException(
                    "Doctor",
                    doctorId
            );
        }
    }

    private void validateTime(
            LocalTime start,
            LocalTime end
    ) {

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Start time must be before end time"
            );
        }
    }

    private ScheduleResponse toResponse(
            DoctorSchedule schedule
    ) {

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getDoctor().getId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getSlotDurationMinutes(),
                schedule.getActive()
        );
    }
}