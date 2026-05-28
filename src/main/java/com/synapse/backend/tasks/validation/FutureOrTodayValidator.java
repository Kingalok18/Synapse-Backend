package com.synapse.backend.tasks.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class FutureOrTodayValidator implements ConstraintValidator<FutureOrToday, LocalDateTime> {

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Null values are fine (due date is optional); use @NotNull if mandatory.
        }
        
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        ZonedDateTime nowIST = ZonedDateTime.now(zoneId);
        LocalDate today = nowIST.toLocalDate();
        LocalDateTime startOfToday = today.atStartOfDay();

        return !value.isBefore(startOfToday);
    }
}
