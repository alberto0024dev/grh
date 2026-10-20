package com.grh.grh.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DataHasherTest {

    private final DataHasher hasher = new DataHasher(Base64.getEncoder().encodeToString(new byte[32]));

    @Test
    void elMismoValorDaSiempreElMismoHash() {
        assertThat(hasher.hash("12345678Z")).isEqualTo(hasher.hash("12345678Z"));
    }

    @Test
    void valoresDistintosDanHashesDistintos() {
        assertThat(hasher.hash("12345678Z")).isNotEqualTo(hasher.hash("12345678X"));
    }

    @Test
    void elHashMideSesentaYCuatroCaracteres() {
        assertThat(hasher.hash("12345678Z")).hasSize(64).matches("[0-9a-f]+");
    }

    @Test
    void conOtraClaveElHashCambia() {
        DataHasher other = new DataHasher(Base64.getEncoder().encodeToString(new byte[]{
                1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}));
        assertThat(other.hash("12345678Z")).isNotEqualTo(hasher.hash("12345678Z"));
    }
}
