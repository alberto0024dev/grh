package com.grh.grh.position;

import com.grh.grh.employee.Employee;
import com.grh.grh.employee.EmployeeRepository;
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
public class PositionService {


    // se sustituye autowired por poner el atributo final y crear un constructor RequiredArgsConstructor.
    // Inyeccion de dependencias por constructor
    private final PositionRepository positionRepository;
    private final UsuarioRepository  usuarioRepository;
    private final EmployeeRepository employeeRepository;

    public PositionResponseDTO create(PositionRequestDTO dto) {
        String name = dto.getName().trim();
        if (positionRepository.existsByNameIgnoreCaseAndJobCategory(name, dto.getJobCategory())) {
            throw new ConflictException("Ya existe el cargo \"" + name + "\" con categoría " + dto.getJobCategory());
        }
        Position saved = positionRepository.save(Position.fromRequest(dto));
        return PositionResponseDTO.fromEntity(saved);
    }

    public PositionResponseDTO update(Long id, PositionRequestDTO dto) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado: " + id));
        String name = dto.getName().trim();
        if (positionRepository.existsByNameIgnoreCaseAndJobCategoryAndIdNot(name, dto.getJobCategory(), id)) {
            throw new ConflictException("Ya existe el cargo \"" + name + "\" con categoría " + dto.getJobCategory());
        }
        position.setName(name);
        position.setJobCategory(dto.getJobCategory());
        return PositionResponseDTO.fromEntity(positionRepository.save(position));
    }

    @Transactional
    public void delete(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado: " + id));

        if (position.getEmployees() != null && !position.getEmployees().isEmpty()) {
            throw new ConflictException("No se puede eliminar el cargo: lo tienen "
                    + position.getEmployees().size() + " empleado(s)");
        }
        positionRepository.deleteById(id);
    }

    public List<PositionResponseDTO> getAll() {
        return positionRepository.findAll().stream()
                .map(PositionResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public PositionResponseDTO getById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado: " + id));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario userLogged = usuarioRepository.findByEmail(email).orElseThrow();

        if (userLogged.getRole() == ApplicationUserRole.ADMIN) {
            return PositionResponseDTO.fromEntity(position);
        }

        if (userLogged.getRole() == ApplicationUserRole.MANAGER) {
            Employee employeeLogged = employeeRepository.findByUsuario(userLogged).orElseThrow();
            boolean hasAccess = employeeLogged.getDepartment() != null
                    && position.getEmployees().stream()
                    .anyMatch(e -> e.getDepartment() != null
                            && e.getDepartment().getId().equals(employeeLogged.getDepartment().getId()));
            if (!hasAccess) {
                throw new ForbiddenAccessException("No tienes acceso a este cargo");
            }
            return PositionResponseDTO.fromEntity(position);
        }
        // Cualquier otro rol (EMPLOYEE): sin acceso
        throw new ForbiddenAccessException("No tienes acceso a este cargo");
    }


}
