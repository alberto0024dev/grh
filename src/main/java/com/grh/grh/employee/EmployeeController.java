package com.grh.grh.employee;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDTO> create(@Valid @RequestBody EmployeeRequestDTO employeeRequestDTO){
        EmployeeResponseDTO employeeResponseDTO = employeeService.createEmployee(employeeRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeResponseDTO); // 201 created + cuerpo
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDTO employeeRequestDTO){
        EmployeeResponseDTO employeeResponseDTO = employeeService.updateEmployee(id, employeeRequestDTO);
        return ResponseEntity.ok(employeeResponseDTO); // 200 operacion correcta devuelve datos.
    }

    @PatchMapping("/me")
    @PreAuthorize("hasAuthority('profile:update')")
    public ResponseEntity<EmployeeResponseDTO> updateMe(@Valid @RequestBody EmployeeProfileDTO employeeProfileDTO){
        EmployeeResponseDTO employeeResponseDTO = employeeService.updateMe(employeeProfileDTO);
        return ResponseEntity.ok(employeeResponseDTO); // 200 operacion correcta devuelve datos.
    }
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('profile:read')")
    public ResponseEntity<EmployeeResponseDTO> getOwnProfile() {
        return ResponseEntity.ok(employeeService.getOwnProfile()); // 200 operacion correcta devuelve datos.
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeResponseDTO>> findAll(){
        List<EmployeeResponseDTO> employeeResponseDTOS = employeeService.getAll();
        return ResponseEntity.ok(employeeResponseDTOS); // 200 operacion correcta devuelve datos.
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<EmployeeResponseDTO> findById(@PathVariable Long id){
        EmployeeResponseDTO employeeResponseDTO = employeeService.getById(id);
        return ResponseEntity.ok(employeeResponseDTO); // 200 operacion correcta devuelve datos.
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id){
        employeeService.deactivate(id);
        return ResponseEntity.noContent().build(); // 204 No Content operación correcta, sin datos que devolver
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activate(@PathVariable Long id){
        employeeService.activate(id);
        return ResponseEntity.noContent().build();
    }
}
