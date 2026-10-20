package com.grh.grh.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LoginRequestDTO {

    @NotBlank(message = "El emaiil es obligatorio")
    @Email(message="Formato de email no valido")
    @Size(max = 200, message = "El email no puede superar 200 caracteres")
    private String email;

    @NotBlank(message="La contraseña es obligatoria")
    @Size(max = 72, message = "La contraseña no puede superar 72 caracteres")
    private String password;

}
