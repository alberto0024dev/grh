package com.grh.grh.position;

import com.grh.grh.department.Department;
import com.grh.grh.employee.Employee;
import com.grh.grh.employee.EmployeeRepository;
import com.grh.grh.exception.ConflictException;
import com.grh.grh.exception.ForbiddenAccessException;
import com.grh.grh.exception.ResourceNotFoundException;
import com.grh.grh.usuario.ApplicationUserRole;
import com.grh.grh.usuario.Usuario;
import com.grh.grh.usuario.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

    @Mock private PositionRepository positionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EmployeeRepository employeeRepository;
    @InjectMocks private PositionService positionService;

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    private Usuario loginAs(String email, ApplicationUserRole role) {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setRole(role);
        u.setActive(true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities()));
        return u;
    }

    private Position position(Long id, String name, JobCategory c) {
        return new Position(id, name, c, List.of());
    }

    private Department department(Long id) {
        Department d = new Department();
        d.setId(id);
        return d;
    }

    private Employee employee(Long id, Department d) {
        Employee e = new Employee();
        e.setId(id);
        e.setDepartment(d);
        e.setPositions(List.of());
        return e;
    }

    // ===== CREATE =====
    @Test
    void create_debeGuardarElCargoConElNombreRecortado() {
        when(positionRepository.save(any(Position.class))).thenAnswer(inv -> inv.getArgument(0));

        PositionResponseDTO result = positionService.create(
                new PositionRequestDTO("  Desarrollador ", JobCategory.SENIOR));

        assertThat(result.getName()).isEqualTo("Desarrollador");
        assertThat(result.getJobCategory()).isEqualTo(JobCategory.SENIOR);
        verify(positionRepository, times(1)).save(any(Position.class));
    }

    @Test
    void create_lanzaConflictSiYaExisteElCargo() {
        when(positionRepository.existsByNameIgnoreCaseAndJobCategory("Desarrollador", JobCategory.SENIOR))
                .thenReturn(true);

        assertThatThrownBy(() -> positionService.create(
                new PositionRequestDTO("Desarrollador", JobCategory.SENIOR)))
                .isInstanceOf(ConflictException.class);
        verify(positionRepository, never()).save(any());
    }

    // ===== GET BY ID =====
    @Test
    void getById_Admin_devuelveElCargo() {
        Usuario admin = loginAs("admin@grh.test", ApplicationUserRole.ADMIN);
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position(1L, "Desarrollador", JobCategory.SENIOR)));
        when(usuarioRepository.findByEmail("admin@grh.test")).thenReturn(Optional.of(admin));

        assertThat(positionService.getById(1L).getName()).isEqualTo("Desarrollador");
        verify(employeeRepository, never()).findByUsuario(any());
    }

    @Test
    void getById_Manager_devuelveCargoSiAlguienDeSuDepartamentoLoTiene() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        Department d = department(1L);
        when(positionRepository.findById(2L)).thenReturn(Optional.of(
                new Position(2L, "Desarrollador", JobCategory.SENIOR, List.of(employee(2L, d)))));
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(employee(1L, d)));

        assertThat(positionService.getById(2L).getName()).isEqualTo("Desarrollador");
    }

    @Test
    void getById_Manager_lanzaForbiddenSiNadieDeSuDepartamentoTieneElCargo() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        when(positionRepository.findById(2L)).thenReturn(Optional.of(
                new Position(2L, "Dev", JobCategory.SENIOR, List.of(employee(2L, department(2L))))));
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(employee(1L, department(1L))));

        assertThatThrownBy(() -> positionService.getById(2L)).isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getById_Manager_sinDepartamento_lanzaForbiddenYNoNPE() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        when(positionRepository.findById(2L)).thenReturn(Optional.of(
                new Position(2L, "Dev", JobCategory.SENIOR, List.of(employee(2L, department(1L))))));
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(employee(1L, null)));

        assertThatThrownBy(() -> positionService.getById(2L)).isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getById_lanzaNotFoundSiNoExiste() {
        when(positionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> positionService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("99");
    }

    // ===== GET ALL =====
    @Test
    void getAll_devuelveTodosLosCargos() {
        when(positionRepository.findAll()).thenReturn(List.of(
                position(1L, "Desarrollador", JobCategory.SENIOR),
                position(2L, "Ingeniero", JobCategory.SENIOR)));

        assertThat(positionService.getAll())
                .extracting(PositionResponseDTO::getName)
                .containsExactly("Desarrollador", "Ingeniero");
    }

    // ===== UPDATE =====
    @Test
    void update_debeActualizarNombreYCategoria() {
        Position existing = position(1L, "Desarrollador", JobCategory.SENIOR);
        when(positionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(positionRepository.save(any(Position.class))).thenAnswer(inv -> inv.getArgument(0));

        PositionResponseDTO result = positionService.update(1L,
                new PositionRequestDTO("  Ingeniero ", JobCategory.LEAD));

        assertThat(result.getName()).isEqualTo("Ingeniero");
        assertThat(result.getJobCategory()).isEqualTo(JobCategory.LEAD);
        verify(positionRepository, times(1)).save(existing);
    }

    @Test
    void update_lanzaNotFoundSiNoExiste() {
        when(positionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> positionService.update(99L, new PositionRequestDTO("X", JobCategory.JUNIOR)))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("99");
        verify(positionRepository, never()).save(any());
    }

    @Test
    void update_lanzaConflictSiOtroCargoTieneElMismoNombreYCategoria() {
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position(1L, "Viejo", JobCategory.JUNIOR)));
        when(positionRepository.existsByNameIgnoreCaseAndJobCategoryAndIdNot("Dev", JobCategory.SENIOR, 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> positionService.update(1L, new PositionRequestDTO("Dev", JobCategory.SENIOR)))
                .isInstanceOf(ConflictException.class);
        verify(positionRepository, never()).save(any());
    }

    // ===== DELETE =====
    @Test
    void delete_eliminaElCargoSinEmpleados() {
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position(1L, "Desarrollador", JobCategory.SENIOR)));

        positionService.delete(1L);

        verify(positionRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_lanzaConflictSiTieneEmpleados() {
        when(positionRepository.findById(1L)).thenReturn(Optional.of(
                new Position(1L, "Desarrollador", JobCategory.SENIOR, List.of(new Employee()))));

        assertThatThrownBy(() -> positionService.delete(1L))
                .isInstanceOf(ConflictException.class).hasMessageContaining("empleado");
        verify(positionRepository, never()).deleteById(any());
    }

    @Test
    void delete_lanzaNotFoundSiNoExiste() {
        when(positionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> positionService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("99");
        verify(positionRepository, never()).deleteById(any());
    }
}