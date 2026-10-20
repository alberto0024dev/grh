package com.grh.grh.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DniValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDni {
    String message() default "El DNI/NIE no es válido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
