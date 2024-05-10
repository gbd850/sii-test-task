package com.test.sii.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.ConstraintComposition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ConstraintComposition()
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Pattern(regexp = "^[0-9a-zA-Z]{3,24}$")
public @interface PromoCodePattern {
    String message() default "Promo code must be a text with 3-24 alphanumeric case-sensitive characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}


