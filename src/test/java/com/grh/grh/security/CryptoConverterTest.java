package com.grh.grh.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class CryptoConverterTest {

    private final CryptoConverter converter = new CryptoConverter(
            Base64.getEncoder().encodeToString(new byte[32]));   // clave de prueba

    @Test
    void cifraYDescifra() {
        String stored = converter.convertToDatabaseColumn("12345678Z");
        assertThat(stored).isNotEqualTo("12345678Z");
        assertThat(converter.convertToEntityAttribute(stored)).isEqualTo("12345678Z");
    }

    @Test
    void dosCifradosDelMismoDatoSonDistintos() {
        assertThat(converter.convertToDatabaseColumn("12345678Z"))
                .isNotEqualTo(converter.convertToDatabaseColumn("12345678Z"));
    }
    @Test
    void falloSiElDatoCifradoFueModificado() {
        String stored = converter.convertToDatabaseColumn("12345678Z");
        byte[] data = Base64.getDecoder().decode(stored);
        data[data.length - 1] ^= 1;                       // cambia un bit
        String tampered = Base64.getEncoder().encodeToString(data);

        assertThatThrownBy(() -> converter.convertToEntityAttribute(tampered))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void conNullDevuelveNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void cifraYDescifraTextoConTildesYEspacios() {
        String text = "Ñandú Pérez 123";
        assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(text))).isEqualTo(text);
    }

    @Test
    void conOtraClaveNoSePuedeDescifrar() {
        CryptoConverter other = new CryptoConverter(Base64.getEncoder().encodeToString(new byte[]{
                1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}));
        String stored = converter.convertToDatabaseColumn("12345678Z");

        assertThatThrownBy(() -> other.convertToEntityAttribute(stored))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rechazaClavesQueNoMidenTreintaYDosBytes() {
        assertThatThrownBy(() -> new CryptoConverter(Base64.getEncoder().encodeToString(new byte[16])))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cabeEnLaColumnaDeCienCaracteres() {
        // El DNI (9) y la seguridad social (12) cifrados deben caber en length = 100
        assertThat(converter.convertToDatabaseColumn("123456789012").length()).isLessThanOrEqualTo(100);
    }
}
