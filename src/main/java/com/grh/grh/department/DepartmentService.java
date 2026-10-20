package com.grh.grh.department;


import com.grh.grh.employee.Employee;
import com.grh.grh.employee.EmployeeRepository;
import com.grh.grh.employee.EmployeeResponseDTO;
import com.grh.grh.exception.ConflictException;
import com.grh.grh.exception.ForbiddenAccessException;
import com.grh.grh.exception.ResourceNotFoundException;
import com.grh.grh.usuario.ApplicationUserRole;
import com.grh.grh.usuario.Usuario;
import com.grh.grh.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmployeeRepository employeeRepository;


    public DepartmentResponseDTO create(DepartmentRequestDTO departmentRequestDTO) {
        checkManagerExists(departmentRequestDTO.getManagerId());
        Department department = Department.fromRequest(departmentRequestDTO);
        Department savedDepartment = departmentRepository.save(department);
        return DepartmentResponseDTO.fromEntity(savedDepartment);
    }


    public DepartmentResponseDTO update(Long id,DepartmentRequestDTO departmentRequestDTO) {
        checkManagerExists(departmentRequestDTO.getManagerId());
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + id));
        updateDepartment(departmentRequestDTO, department);
        return DepartmentResponseDTO.fromEntity(departmentRepository.save(department));
    }

    private void updateDepartment(DepartmentRequestDTO departmentRequestDTO, Department department) {
        department.setName(departmentRequestDTO.getName().trim());
        department.setBuilding(departmentRequestDTO.getBuilding().trim());
        department.setFloor(departmentRequestDTO.getFloor());
        department.setAddress(departmentRequestDTO.getAddress().trim());
        department.setStatus(departmentRequestDTO.getStatus());
        department.setContactPhone(departmentRequestDTO.getContactPhone());
        department.setAnnualBudget(departmentRequestDTO.getAnnualBudget());
        department.setManagerId(departmentRequestDTO.getManagerId());
    }

    @Transactional
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + id));
        if (department.getEmployees() != null && !department.getEmployees().isEmpty()) {
            throw new ConflictException("No se puede eliminar el departamento: tiene "
                    + department.getEmployees().size() + " empleado(s) asignado(s)");
        }
        departmentRepository.delete(department);
    }

    public List<DepartmentResponseDTO> getAll() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponseDTO::fromEntity)
                .toList();
    }

    // Lanza 403 si el usuario no es ADMIN ni el MANAGER de ese departamento
    private void checkDepartmentAccess(Usuario userLogged, Department department) {
        if (userLogged.getRole() == ApplicationUserRole.ADMIN) return;

        if (userLogged.getRole() == ApplicationUserRole.MANAGER) {
            Employee employeeLogged = employeeRepository.findByUsuario(userLogged).orElseThrow();
            if (employeeLogged.getDepartment() != null
                    && employeeLogged.getDepartment().getId().equals(department.getId())) {
                return;
            }
        }
        throw new ForbiddenAccessException("No tienes acceso a este departamento");
    }

    @Transactional
    public DepartmentResponseDTO getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + id));
        String emailLogged = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario userLogged = usuarioRepository.findByEmail(emailLogged).orElseThrow();

        checkDepartmentAccess(userLogged, department);
        return DepartmentResponseDTO.fromEntity(department);
    }

    @Transactional
    public List<EmployeeResponseDTO> getEmployeesByDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + id));
        String emailLogged = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario userLogged = usuarioRepository.findByEmail(emailLogged).orElseThrow();

        checkDepartmentAccess(userLogged, department);
        return department.getEmployees().stream()
                .map(EmployeeResponseDTO::fromEntity)
                .toList();
    }
    private void checkManagerExists(Long managerId) {
        if (managerId !=null && !employeeRepository.existsById(managerId)) {
            throw new ResourceNotFoundException("El manager indicado no existe: " + managerId);
        }
    }



}
