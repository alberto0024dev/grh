package com.grh.grh.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DniValidatorTest {

    private final DniValidator validator = new DniValidator();

    @Test void aceptaDniCorrecto()  { assertThat(validator.isValid("12345678Z", null)).isTrue(); }
    @Test void aceptaMinusculas()   { assertThat(validator.isValid("12345678z", null)).isTrue(); }
    @Test void aceptaNie()          { assertThat(validator.isValid("X1234567L", null)).isTrue(); }
    @Test void rechazaLetraErronea(){ assertThat(validator.isValid("12345678K", null)).isFalse(); }
    @Test void rechazaFormatoRaro() {
        assertThat(validator.isValid("abcdefghi", null)).isFalse();
        assertThat(validator.isValid("1234567Z", null)).isFalse();
    }
    @Test void nuloOVacioLoGestionaNotBlank() {
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("  ", null)).isTrue();
    }
}
