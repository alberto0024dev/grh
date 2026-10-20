package com.grh.grh.employee;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]*$", message = "El nombre solo admite letras, espacios, punto, apóstrofo y guion")
    private String name;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 50, message = "El primer apellido no puede superar 50 caracteres")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]*$", message = "El apellido solo admite letras, espacios, punto, apóstrofo y guion")
    private String firstSurname;

    @Size(max = 50, message = "El segundo apellido no puede superar 50 caracteres")
    @Pattern(regexp = "^$|^[\\p{L}][\\p{L} .'-]*$", message = "El apellido solo admite letras, espacios, punto, apóstrofo y guion")
    private String secondSurname;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    private String address;

    @Size(max = 100, message = "El contacto de emergencia no puede superar 100 caracteres")
    private String emergencyContact;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
    private String phoneNumber;

    @NotNull(message = "El departamento es obligatorio")
    @Positive(message = "El departamento no es válido")
    private Long departmentId;

    @NotEmpty(message = "Debe tener al menos un cargo")
    private List<@NotNull(message = "Id de cargo no válido") @Positive(message = "Id de cargo no válido") Long> positionIds;
}