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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    @Mock
    DepartmentRepository departmentRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    DepartmentService departmentService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Usuario loginAs(String email, ApplicationUserRole role) {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setRole(role);
        u.setActive(true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities()));
        return u;
    }

    private Department dept(Long id) {
        Department d = new Department();
        d.setId(id);
        return d;
    }

    private Employee emp(Long id, String name, Department d) {
        Employee e = new Employee();
        e.setId(id);
        e.setName(name);
        e.setDepartment(d);
        e.setPositions(List.of());
        return e;
    }

    @Test
    void create_debeCrearYDevolverDepartamento() {
        DepartmentRequestDTO dto = new DepartmentRequestDTO("  departamento ", "edificio", 2, "plaza",
                Status.AVAILABLE, "912345678", BigDecimal.valueOf(23411.2), 1L);

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentResponseDTO result = departmentService.create(dto);

        assertThat(result.getName()).isEqualTo("departamento");
        assertThat(result.getBuilding()).isEqualTo("edificio");
        assertThat(result.getFloor()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(Status.AVAILABLE);
        assertThat(result.getAnnualBudget()).isEqualByComparingTo("23411.2");
        assertThat(result.getManagerId()).isEqualTo(1L);
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void update_debeActualizarTodosLosCampos() {
        Department existing = new Department(1L, "Viejo", "Edificio viejo", 9, "Calle vieja",
                Status.MAINTENANCE, "900000000", BigDecimal.valueOf(1000), 5L, List.of());
        DepartmentRequestDTO dto = new DepartmentRequestDTO(" Nuevo ", "Edificio nuevo", 2, "Plaza",
                Status.AVAILABLE, "912345678", BigDecimal.valueOf(23411.2), 1L);

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentResponseDTO result = departmentService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("Nuevo");
        assertThat(result.getBuilding()).isEqualTo("Edificio nuevo");
        assertThat(result.getFloor()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(Status.AVAILABLE);
        assertThat(result.getAnnualBudget()).isEqualByComparingTo("23411.2");
        assertThat(result.getManagerId()).isEqualTo(1L);
        verify(departmentRepository, times(1)).save(existing);
    }

    // todo: NO PASA EL TEST.

    @Test
    void delete_debeEliminarDepartamentoSinEmpleados() {
        Department department = new Department(1L, "departamento", "edificio", 2, "plaza",
                Status.AVAILABLE, "912345678", BigDecimal.valueOf(23411.2), 1L, List.of());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        departmentService.delete(1L);

        verify(departmentRepository, times(1)).delete(department);
    }

    @Test
    void getAll_debeDevolverDepartamentos(){
        Department department = new Department(1L,"departamento","edificio",2,"plaza",
                Status.AVAILABLE,"218731212", BigDecimal.valueOf(23411.2),1L, List.of());
        Department department2 = new Department(2L,"Prueba","edificio",2,"plaza",
                Status.AVAILABLE,"218731212", BigDecimal.valueOf(23411.2),1L, List.of());

        when(departmentRepository.findAll()).thenReturn(List.of(department,department2));

        List<DepartmentResponseDTO> result = departmentService.getAll();

        assertThat(result).extracting(DepartmentResponseDTO::getName)
                .containsExactly("departamento", "Prueba");
    }

    @Test
    void getById_Admin_debeDevolverDepartamento() {
        Usuario admin = loginAs("admin@grh.test", ApplicationUserRole.ADMIN);
        Department department = new Department(1L, "departamento", "edificio", 2, "plaza",
                Status.AVAILABLE, "912345678", BigDecimal.valueOf(23411.2), 1L, List.of());

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(usuarioRepository.findByEmail("admin@grh.test")).thenReturn(Optional.of(admin));

        DepartmentResponseDTO result = departmentService.getById(1L);

        assertThat(result.getName()).isEqualTo("departamento");
        assertThat(result.getFloor()).isEqualTo(2);
        verify(employeeRepository, never()).findByUsuario(any());   // el admin no necesita ficha
    }

    @Test
    void getById_Manager_debeDevolverSuDepartamento() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        Department department = dept(1L);
        Employee me = new Employee();
        me.setDepartment(department);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(me));

        assertThat(departmentService.getById(1L).getId()).isEqualTo(1L);
    }
    @Test
    void getById_Manager_sinDepartamento_lanzaForbiddenYNoNPE() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept(1L)));
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(new Employee()));

        assertThatThrownBy(() -> departmentService.getById(1L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getById_Manager_otroDepartamento_lanzaForbidden() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        Employee me = new Employee();
        me.setDepartment(dept(1L));

        when(departmentRepository.findById(2L)).thenReturn(Optional.of(dept(2L)));
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(me));

        assertThatThrownBy(() -> departmentService.getById(2L))
                .isInstanceOf(ForbiddenAccessException.class);
    }
    @Test
    void getById_Employee_lanzaForbidden() {
        Usuario u = loginAs("ana@grh.test", ApplicationUserRole.EMPLOYEE);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept(1L)));
        when(usuarioRepository.findByEmail("ana@grh.test")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> departmentService.getById(1L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getEmployeesByDepartment_Manager_sinDepartamento_lanzaForbiddenYNoNPE() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        Department d = dept(1L);
        d.setEmployees(List.of());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(d));
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(new Employee()));

        assertThatThrownBy(() -> departmentService.getEmployeesByDepartment(1L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getById_NoExiste_lanzaNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getEmployeesByDepartment_Admin_debeDevolverEmpleados() {
        Usuario admin = loginAs("admin@grh.test", ApplicationUserRole.ADMIN);
        Department d = dept(1L);
        d.setEmployees(List.of(emp(1L, "Ana", d), emp(2L, "Luis", d)));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(d));
        when(usuarioRepository.findByEmail("admin@grh.test")).thenReturn(Optional.of(admin));

        List<EmployeeResponseDTO> result = departmentService.getEmployeesByDepartment(1L);

        assertThat(result).extracting(EmployeeResponseDTO::getName).containsExactly("Ana", "Luis");
        verify(employeeRepository, never()).findByUsuario(any());
    }

    @Test
    void getEmployeesByDepartment_Manager_debeDevolverEmpleados() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        Department d = dept(1L);
        d.setEmployees(List.of(emp(1L, "Ana", d), emp(2L, "Luis", d)));
        Employee me = emp(3L, "Pedro", d);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(d));
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(me));

        assertThat(departmentService.getEmployeesByDepartment(1L)).hasSize(2);
    }
    @Test
    void getEmployeesByDepartment_Manager_otroDepartamento_lanzaForbidden() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        Employee me = emp(3L, "Pedro", dept(1L));
        Department other = dept(2L);
        other.setEmployees(List.of());

        when(departmentRepository.findById(2L)).thenReturn(Optional.of(other));
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(me));

        assertThatThrownBy(() -> departmentService.getEmployeesByDepartment(2L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getEmployeesByDepartment_NoExiste_lanzaNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getEmployeesByDepartment(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    @Test
    void delete_debeLanzarConflictCuandoTieneEmpleados() {
        Department department = new Department(1L, "departamento", "edificio", 2, "plaza",
                Status.AVAILABLE, "218731212", BigDecimal.valueOf(23411.2), 1L, List.of(new Employee()));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("empleado");
        verify(departmentRepository, never()).delete(any());

    }

    @Test
    void create_debeLanzarExcepcionSiElManagerNoExiste() {
        DepartmentRequestDTO dto = new DepartmentRequestDTO("departamento", "edificio", 2, "plaza",
                Status.AVAILABLE, "912345678", BigDecimal.valueOf(23411.2), 99L);
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> departmentService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void update_debeLanzarExcepcionSiElManagerNoExiste() {
        DepartmentRequestDTO dto = new DepartmentRequestDTO("departamento", "edificio", 2, "plaza",
                Status.AVAILABLE, "912345678", BigDecimal.valueOf(23411.2), 99L);
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> departmentService.update(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void update_debePermitirQuitarElManager() {
        Department department = new Department(1L, "departamento", "edificio", 2, "plaza",
                Status.AVAILABLE, "912345678", BigDecimal.valueOf(23411.2), 5L, List.of());
        DepartmentRequestDTO dto = new DepartmentRequestDTO("departamento", "edificio", 2, "plaza",
                Status.AVAILABLE, "912345678", BigDecimal.valueOf(23411.2), null);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentResponseDTO result = departmentService.update(1L, dto);

        assertThat(result.getManagerId()).isNull();
        verify(employeeRepository, never()).existsById(any());
    }

    @Test
    void create_debeCrearDepartamentoSinManager() {
        DepartmentRequestDTO dto = new DepartmentRequestDTO("departamento", "edificio", 2, "plaza",
                Status.AVAILABLE, "912345678", BigDecimal.valueOf(23411.2), null);

        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentResponseDTO result = departmentService.create(dto);

        assertThat(result.getName()).isEqualTo("departamento");
        assertThat(result.getManagerId()).isNull();
        verify(employeeRepository, never()).existsById(any());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void update_debeLanzarExcepcionSiElDepartamentoNoExiste() {
        DepartmentRequestDTO dto = new DepartmentRequestDTO("departamento", "edificio", 2, "plaza",
                Status.AVAILABLE, "912345678", BigDecimal.valueOf(23411.2), null);
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(departmentRepository, never()).save(any());
    }


}
