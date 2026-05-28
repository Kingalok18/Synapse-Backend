package com.synapse.backend.notes.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class TagsValidator implements ConstraintValidator<ValidTags, List<String>> {

    @Override
    public boolean isValid(List<String> tags, ConstraintValidatorContext context) {
        if (tags == null) {
            return true; // Null lists are fine; use @NotNull if null is forbidden
        }
        for (String tag : tags) {
            if (tag == null || tag.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
