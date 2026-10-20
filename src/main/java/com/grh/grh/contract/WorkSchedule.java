package com.grh.grh.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Embeddable
@Data
public class WorkSchedule {

    @Column(nullable = false)
    @NotNull(message = "Las horas semanales son obligatorias")
    @Min(value = 1, message = "Las horas semanales deben ser al menos 1")
    @Max(value = 60, message = "Las horas semanales no pueden superar 60")
    private Integer weeklyHours;

    @Column(nullable = false)
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @Column(nullable = false)
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    @Column(nullable = false)
    @NotNull(message = "Los días libres son obligatorios")
    @Min(value = 0, message = "Los días libres no pueden ser negativos")
    @Max(value = 7, message = "Los días libres no pueden superar 7")
    private Integer daysOff;

    @JsonIgnore
    @AssertTrue(message = "La hora de fin debe ser posterior a la de inicio")
    public boolean isTimeRangeValid() {
        if (startTime == null || endTime == null) return true;   // ya lo avisan los @NotNull
        return endTime.isAfter(startTime);
    }
}