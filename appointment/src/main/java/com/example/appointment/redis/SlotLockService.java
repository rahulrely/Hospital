package com.example.appointment.redis;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SlotLockService {

    private static final Duration HOLD_DURATION =
            Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public boolean acquire(
            Long doctorId,
            java.time.LocalDate date,
            java.time.LocalTime time,
            Long patientId
    ) {

        String key = buildKey(
                doctorId,
                date,
                time
        );

        Boolean acquired =
                redisTemplate.opsForValue().setIfAbsent(
                        key,
                        patientId.toString(),
                        HOLD_DURATION
                );

        return Boolean.TRUE.equals(acquired);
    }

    public void release(
            Long doctorId,
            java.time.LocalDate date,
            java.time.LocalTime time
    ) {

        redisTemplate.delete(
                buildKey(
                        doctorId,
                        date,
                        time
                )
        );
    }

    public boolean isHeld(
            Long doctorId,
            java.time.LocalDate date,
            java.time.LocalTime time
    ) {

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(
                        buildKey(
                                doctorId,
                                date,
                                time
                        )
                )
        );
    }

    private String buildKey(
            Long doctorId,
            java.time.LocalDate date,
            java.time.LocalTime time
    ) {

        return String.format(
                "appointment:slot:%d:%s:%s",
                doctorId,
                date,
                time
        );
    }
}