package com.grh.grh.position;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {


    private final PositionService positionService;

    // Acuerdate que si empieza por 2 el codgio http signfica que tuvo exito.

    // crear-> Post (tienees que enviar datos).
    // ¿si falla eso lo controlaria el globalExceptionHandler? ¿por eso no se controla fallo aqui? o esta mal?
    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PositionResponseDTO> create(@Valid @RequestBody PositionRequestDTO positionRequestDTO) {
        PositionResponseDTO responseDTO= positionService.create(positionRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO); // 201 Created recurso creado correctamente
    }

    // como recibo el id?
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PositionResponseDTO> update(@Valid @RequestBody PositionRequestDTO positionRequestDTO,
                                                      @PathVariable Long id) {
        PositionResponseDTO positionResponseDTO = positionService.update(id, positionRequestDTO);
        return ResponseEntity.ok(positionResponseDTO); // 200 OK Operacion correcta devuelve datos

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return ResponseEntity.noContent().build(); // 204 No Content operación correcta, sin datos que devolver
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PositionResponseDTO> getById(@PathVariable Long id) {
        PositionResponseDTO responseDTO = positionService.getById(id);
        return ResponseEntity.ok(responseDTO); // 200 OK Operacion correcta devuelve datos
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<PositionResponseDTO>> getAll() {
        List<PositionResponseDTO> responseDTO = positionService.getAll();
        return ResponseEntity.ok(responseDTO); // 200 OK Operacion correcta deevuelve datos.
    }


}
