package com.grh.grh.employee;


import com.grh.grh.usuario.ApplicationUserRole;
import com.grh.grh.validation.ValidDni;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]*$", message = "El nombre solo admite letras, espacios, punto, apóstrofo y guion")
    private String name;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 50, message = "El primer apellido no puede superar 50 caracteres")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]*$", message = "El apellido solo admite letras, espacios, punto, apóstrofo y guion")
    private String firstSurname;

    // Opcional: el patrón admite vacío, pero no números ni símbolos
    @Size(max = 50, message = "El segundo apellido no puede superar 50 caracteres")
    @Pattern(regexp = "^$|^[\\p{L}][\\p{L} .'-]*$", message = "El apellido solo admite letras, espacios, punto, apóstrofo y guion")
    private String secondSurname;

    @NotBlank(message = "El dni es obligatorio")
    @ValidDni(message = "El DNI o NIE no es válido ")
    private String idNumber;

    @NotBlank(message = "La direccion es obligatoria")
    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    private String address;


    @Size(max = 100, message = "El contacto de emergencia no puede superar 100 caracteres")
    private String emergencyContact;

    @NotBlank(message = "El telefono es obligatorio")
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
    private String phoneNumber;

    @NotBlank(message = "El email es obligatorio")
    @Size(max = 200, message = "El email no puede superar 200 caracteres")
    @Email(message = "Formato de email incorrecto")
    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$", message = "Formato de email incorrecto")
    private String email;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser pasada")
    private LocalDate birthDate;

    @NotBlank(message = "El numero de la seguridad social es obligatorio")
    @Pattern(regexp = "\\d{12}", message = "La seguridad social debe tener 12 dígitos")
    private String socialSecurityNumber;

    @NotNull
    private ApplicationUserRole role;


    @NotNull(message = "El id del departamento es obligatorio")
    private Long departmentId;

    @NotEmpty(message = "El id de la posicion es obligatorio")
    private List<@NotNull(message = "Id de cargo no válido") @Positive(message = "Id de cargo no válido") Long> positionIds;

    // La edad laboral mínima en España es 16 años
    @AssertTrue(message = "El empleado debe tener entre 16 y 100 años")
    public boolean isBirthDateInRange() {
        if (birthDate == null) return true;   // ya lo avisa @NotNull
        LocalDate today = LocalDate.now();
        return !birthDate.isAfter(today.minusYears(16)) && birthDate.isAfter(today.minusYears(100));
    }

}
