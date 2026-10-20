package com.grh.grh.employee;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeProfileDTO {

    @Pattern(regexp = ".*\\S.*", message = "La dirección no puede estar vacía")
    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    private String address;

    @Size(max = 100, message = "El contacto de emergencia no puede superar 100 caracteres")
    private String emergencyContact;

    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
    private String phoneNumber;




}
