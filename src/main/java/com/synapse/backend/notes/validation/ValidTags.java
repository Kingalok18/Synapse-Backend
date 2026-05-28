package com.synapse.backend.notes.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TagsValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTags {
    String message() default "Tags must not contain null or blank values";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
