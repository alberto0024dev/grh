package com.grh.grh.contract;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContractResponseDTO> create(@Valid @RequestBody ContractRequestDTO contractRequestDTO) {
        ContractResponseDTO contractResponseDTO = contractService.create(contractRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(contractResponseDTO); // 201 created + cuerpo
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContractResponseDTO> update(@Valid @RequestBody ContractUpdateDTO contractUpdateDTO,
                                                      @PathVariable Long id) {
        ContractResponseDTO contractResponseDTO = contractService.update(id,contractUpdateDTO);
        return ResponseEntity.ok(contractResponseDTO);// 200 operacion correcta devuelve datos.
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return ResponseEntity.noContent().build();// 204 No Content operación correcta, sin datos que devolver
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContractResponseDTO>> getAll() {
        List<ContractResponseDTO> contractResponseDTOS = contractService.getAll();
        return ResponseEntity.ok(contractResponseDTOS); // 200 operacion correcta devuelve datos.
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ContractResponseDTO> getById(@PathVariable Long id) {
        ContractResponseDTO contractResponseDTO = contractService.getById(id);
        return ResponseEntity.ok(contractResponseDTO); // 200 operacion correcta devuelve datos.
    }

    @GetMapping("/{id}/employee")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ContractResponseDTO> getByEmployeeId(@PathVariable Long id) {
        ContractResponseDTO contractResponseDTO = contractService.getByEmployeeId(id);
        return ResponseEntity.ok(contractResponseDTO); // 200 operacion correcta devuelve datos.
    }
}
