package com.grh.grh.position;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Pattern(regexp = "^[\\p{L}0-9][\\p{L}0-9 .,'/&()+#-]*$",
            message = "El nombre contiene caracteres no permitidos")
    private String name;

    @NotNull(message = "La categoria es obligatoria")
    private JobCategory jobCategory;
}
