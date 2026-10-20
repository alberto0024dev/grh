package com.grh.grh.department;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String name;

    @NotBlank(message = "El edificio es obligatorio")
    @Size(max = 100, message = "El edificio no puede superar 100 caracteres")
    private String building;

    @NotNull
    @Min(value = 0, message = "La planta no puede ser negativa")
    @Max(value = 100, message = "La planta no puede superar 100")
    private int floor;

    @NotBlank(message = "La direccion es obligatoria")
    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    private String address;

    @NotNull(message = "El estado es obligatorio")
    private Status status;

    // Opcional: si viene, debe tener entre 9 y 15 dígitos (admite + inicial). "" no pasa. (15 digitos porque es el maximo en otros paises).
    @Pattern(regexp = "^\\+?\\d{9,15}$", message = "El teléfono debe tener entre 9 y 15 dígitos")
    private String contactPhone;

    @NotNull(message = "El presupuesto anual es obligatorio")
    @Positive(message = "El presupuesto anual debe ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "El presupuesto admite hasta 10 enteros y 2 decimales")
    private BigDecimal annualBudget;

    @Positive(message = "El id del manager no es válido")
    private Long managerId;

}
