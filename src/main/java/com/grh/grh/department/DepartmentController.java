package com.grh.grh.department;


import com.grh.grh.employee.EmployeeResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponseDTO> create(@Valid @RequestBody DepartmentRequestDTO departmentRequestDTO){
        DepartmentResponseDTO departmentResponseDTO = departmentService.create(departmentRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentResponseDTO); // 201 created + cuerpo
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponseDTO> update(@Valid @RequestBody DepartmentRequestDTO departmentRequestDTO,
                                                        @PathVariable Long id){
        DepartmentResponseDTO departmentResponseDTO = departmentService.update(id, departmentRequestDTO);
        return ResponseEntity.ok(departmentResponseDTO); // 200 operacion correcta devuelve datos.

    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        departmentService.delete(id);
        return ResponseEntity.noContent().build(); // 204 No Content operación correcta, sin datos que devolver
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentResponseDTO>> getAll(){
        List<DepartmentResponseDTO> departmentResponseDTOS=departmentService.getAll();
        return ResponseEntity.ok(departmentResponseDTOS); //  // 200 OK Operacion correcta deevuelve datos.
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DepartmentResponseDTO> getById(@PathVariable Long id){
        DepartmentResponseDTO departmentResponseDTO=departmentService.getById(id);
        return ResponseEntity.ok(departmentResponseDTO);  // 200 OK Operacion correcta deevuelve datos.
    }

    @GetMapping("/{id}/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesByDepartment(@PathVariable Long id){
        List<EmployeeResponseDTO> employeeResponseDTOS=departmentService.getEmployeesByDepartment(id);
        return ResponseEntity.ok(employeeResponseDTOS); // 200 OK Operacion correcta deevuelve datos.
    }

}
