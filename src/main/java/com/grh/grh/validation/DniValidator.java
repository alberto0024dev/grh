package com.grh.grh.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class DniValidator implements ConstraintValidator<ValidDni, String> {

    private static final String LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final Pattern FORMAT = Pattern.compile("^([0-9]{8}|[XYZ][0-9]{7})[A-Z]$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;   // de eso se encarga @NotBlank

        String dni = value.trim().toUpperCase();
        if (!FORMAT.matcher(dni).matches()) return false;

        // En un NIE, la letra inicial se cambia por un número
        String digits = dni.substring(0, dni.length() - 1)
                .replace('X', '0').replace('Y', '1').replace('Z', '2');
        char expected = LETTERS.charAt(Integer.parseInt(digits) % 23);

        return dni.charAt(dni.length() - 1) == expected;
    }
}
