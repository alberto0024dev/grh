package com.grh.grh.contract;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractUpdateDTO {

    @NotNull(message = "El tipo de contrato es obligatorio")
    private ContractType contractType;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate commencementDate;

    private LocalDate expiryDate;

    @NotNull(message = "Los días de vacaciones son obligatorios")
    @Min(value = 0, message = "Los días de vacaciones no pueden ser negativos")
    @Max(value = 60, message = "Los días de vacaciones no pueden superar 60")
    private Integer vacationDays;

    @NotNull(message = "El salario es obligatorio")
    @Positive(message = "El salario debe ser mayor que 0")
    @Digits(integer = 8, fraction = 2, message = "El salario admite hasta 8 enteros y 2 decimales")
    private BigDecimal salary;

    @NotNull(message = "El lugar de trabajo es obligatorio")
    private WorkPlace workPlace;

    @NotNull(message = "El horario es obligatorio")
    @Valid
    private WorkSchedule workSchedule;

    @AssertTrue(message = "Un contrato permanente no lleva fecha de fin; el resto sí, y debe ser posterior al inicio")
    public boolean isExpiryDateValid() {
        if (contractType == null || commencementDate == null) return true;   // ya lo avisan los @NotNull
        if (contractType == ContractType.PERMANENT) return expiryDate == null;
        return expiryDate != null && expiryDate.isAfter(commencementDate);
    }
}