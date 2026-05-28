package com.synapse.backend.tasks.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FutureOrTodayValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureOrToday {
    String message() default "Due date must be today or in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
