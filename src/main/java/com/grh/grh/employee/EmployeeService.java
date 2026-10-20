package com.grh.grh.employee;

import com.grh.grh.department.Department;
import com.grh.grh.department.DepartmentRepository;
import com.grh.grh.exception.BadRequestException;
import com.grh.grh.exception.ConflictException;
import com.grh.grh.exception.ForbiddenAccessException;
import com.grh.grh.exception.ResourceNotFoundException;
import com.grh.grh.position.Position;
import com.grh.grh.position.PositionRepository;
import com.grh.grh.security.DataHasher;
import com.grh.grh.usuario.ApplicationUserRole;
import com.grh.grh.usuario.Usuario;
import com.grh.grh.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final DataHasher hasher;
    @Value("${app.email-domain}")
    private String emailDomain;


    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (!email.endsWith("@" + emailDomain)) {
            throw new BadRequestException("El email debe ser del dominio @" + emailDomain);
        }

        Employee employee = Employee.toEntity(dto);

        // Duplicados: DNI y seguridad social (por hash) y email (en empleados y en usuarios)
        String idHash = hasher.hash(employee.getIdNumber());
        String ssHash = hasher.hash(employee.getSocialSecurityNumber());
        if (employeeRepository.existsByIdNumberHash(idHash)) {
            throw new ConflictException("Ya existe un empleado con ese DNI");
        }
        if (employeeRepository.existsBySocialSecurityHash(ssHash)) {
            throw new ConflictException("Ya existe un empleado con esa seguridad social");
        }
        if (employeeRepository.existsByEmail(email) || usuarioRepository.existsByEmail(email)) {
            throw new ConflictException("Ya existe un usuario con ese email");
        }
        employee.setIdNumberHash(idHash);
        employee.setSocialSecurityHash(ssHash);

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado"));
        employee.setDepartment(department);
        employee.setPositions(findPositions(dto.getPositionIds()));

        String temporaryPassword = Usuario.generatePassword();
        Usuario userCreated = Usuario.create(employee.getEmail(), temporaryPassword, dto.getRole(), passwordEncoder);
        employee.setUsuario(usuarioRepository.save(userCreated));

        Employee saved = employeeRepository.save(employee);
        EmployeeResponseDTO response = EmployeeResponseDTO.fromEntity(saved);
        response.setTemporaryPassword(temporaryPassword);
        return response;
    }

    @Transactional
    public void deactivate(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + id));
        employee.setActive(false);
        if (employee.getUsuario() != null) {
            employee.getUsuario().setActive(false);
        }
        employeeRepository.save(employee);
    }
    @Transactional
    public void activate(Long id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + id));
        employee.setActive(true);
        if(employee.getUsuario() != null) {
            employee.getUsuario().setActive(true);
        }
        employeeRepository.save(employee);
    }

    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeUpdateDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + id));

        employee.setName(dto.getName().trim());
        employee.setFirstSurname(dto.getFirstSurname().trim());
        employee.setSecondSurname(Employee.clean(dto.getSecondSurname()));
        employee.setAddress(dto.getAddress().trim());
        employee.setEmergencyContact(Employee.clean(dto.getEmergencyContact()));
        employee.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado"));
            employee.setDepartment(department);
        } else {
            employee.setDepartment(null);
        }
        employee.setPositions(findPositions(dto.getPositionIds()));
        return EmployeeResponseDTO.fromEntity(employeeRepository.save(employee));
    }
    @Transactional
    public EmployeeResponseDTO updateMe(EmployeeProfileDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario userLogged = usuarioRepository.findByEmail(email)
                .orElseThrow();
        Employee employee = employeeRepository.findByUsuario(userLogged)
                .orElseThrow();
        if (dto.getAddress() != null) {
            employee.setAddress(dto.getAddress().trim());
        }
        if (dto.getEmergencyContact() != null) {
            employee.setEmergencyContact(Employee.clean(dto.getEmergencyContact()));
        }
        if (dto.getPhoneNumber() != null) {
            employee.setPhoneNumber(dto.getPhoneNumber());
        }
        return  EmployeeResponseDTO.fromEntity(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponseDTO getOwnProfile(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario userLogged = usuarioRepository.findByEmail(email)
                .orElseThrow();
        Employee employee = employeeRepository.findByUsuario(userLogged)
                .orElseThrow();
        return EmployeeResponseDTO.fromEntity(employee);
    }

    @Transactional
    public List<EmployeeResponseDTO> getAll(){
        return employeeRepository.findAll().stream()
                .map(EmployeeResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public EmployeeResponseDTO getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + id));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario userLogged = usuarioRepository.findByEmail(email).orElseThrow();

        if (userLogged.getRole() == ApplicationUserRole.ADMIN) {
            return EmployeeResponseDTO.fromEntity(employee);
        }

        Employee employeeLogged = employeeRepository.findByUsuario(userLogged).orElseThrow();
        boolean isOwn = employeeLogged.getId() != null && employeeLogged.getId().equals(employee.getId());
        boolean sameDepartment = userLogged.getRole() == ApplicationUserRole.MANAGER
                && employee.getDepartment() != null
                && employeeLogged.getDepartment() != null
                && employeeLogged.getDepartment().getId().equals(employee.getDepartment().getId());

        if (!isOwn && !sameDepartment) {
            throw new ForbiddenAccessException("No tienes acceso a este empleado");
        }
        return EmployeeResponseDTO.fromEntity(employee);
    }

    private List<Position> findPositions(List<Long> ids) {
        List<Long> distinct = ids.stream().distinct().toList();
        List<Position> found = positionRepository.findByIdIn(distinct);
        if (found.size() != distinct.size()) {
            throw new ResourceNotFoundException("Alguno de los cargos indicados no existe");
        }
        return found;
    }
}
