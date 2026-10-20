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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private DataHasher hasher;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(employeeService, "emailDomain", "grh.test");
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ===================== AUXILIARES =====================

    private EmployeeRequestDTO validDto() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("Ana");
        dto.setFirstSurname("García");
        dto.setIdNumber("12345678z");            // minúscula a propósito: toEntity la pasa a mayúscula
        dto.setAddress("Calle Mayor 1");
        dto.setPhoneNumber("612345678");
        dto.setEmail("Ana@Grh.test");
        dto.setBirthDate(LocalDate.of(1995, 5, 10));
        dto.setSocialSecurityNumber("123456789012");
        dto.setRole(ApplicationUserRole.EMPLOYEE);
        dto.setDepartmentId(1L);
        dto.setPositionIds(List.of(1L));
        return dto;
    }

    private EmployeeUpdateDTO updateDto() {
        EmployeeUpdateDTO dto = new EmployeeUpdateDTO();
        dto.setName("Juan Actualizado");
        dto.setFirstSurname("Nuevo");
        dto.setSecondSurname("López");
        dto.setPhoneNumber("611111111");
        dto.setAddress("Calle Nueva 1");
        dto.setEmergencyContact("Contacto nuevo");
        dto.setDepartmentId(1L);
        dto.setPositionIds(List.of(1L));
        return dto;
    }

    // Deja logado a un usuario y lo devuelve
    private Usuario loginAs(String email, ApplicationUserRole role) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setRole(role);
        usuario.setActive(true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
        return usuario;
    }

    // Logado + simula findByEmail y, si se pasa ficha, findByUsuario. Para ADMIN, ficha = null
    private Usuario loginWithProfile(String email, ApplicationUserRole role, Employee profile) {
        Usuario usuario = loginAs(email, role);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        if (profile != null) {
            when(employeeRepository.findByUsuario(usuario)).thenReturn(Optional.of(profile));
        }
        return usuario;
    }

    private Department department(Long id) {
        Department department = new Department();
        department.setId(id);
        return department;
    }

    // Con lista de cargos vacía, porque fromEntity la lee
    private Employee employee(Long id, String name, Department department) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setName(name);
        employee.setDepartment(department);
        employee.setPositions(List.of());
        return employee;
    }

    private Position position(Long id) {
        Position position = new Position();
        position.setId(id);
        return position;
    }

    // ===================== CREATE =====================

    @Test
    void create_debeDevolverEmpleado() {
        when(hasher.hash(any())).thenReturn("hash");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department(1L)));
        when(positionRepository.findByIdIn(any())).thenReturn(List.of(position(1L)));
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponseDTO result = employeeService.createEmployee(validDto());

        assertThat(result.getEmail()).isEqualTo("ana@grh.test");          // normalizado a minúsculas
        assertThat(result.getDepartment().getId()).isEqualTo(1L);
        assertThat(result.getTemporaryPassword()).hasSize(12);
        verify(employeeRepository, times(1)).save(any());
        verify(usuarioRepository, times(1)).save(any());
    }

    @Test
    void create_debeLanzarConflictSiElDniYaExiste() {
        when(hasher.hash(any())).thenReturn("hash");
        when(employeeRepository.existsByIdNumberHash("hash")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(validDto()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("DNI");
        verify(employeeRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void create_debeLanzarConflictSiLaSeguridadSocialYaExiste() {
        when(hasher.hash(any())).thenReturn("hash");
        when(employeeRepository.existsBySocialSecurityHash("hash")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(validDto()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("seguridad social");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_debeLanzarConflictSiElEmailYaExiste() {
        when(hasher.hash(any())).thenReturn("hash");
        when(usuarioRepository.existsByEmail("ana@grh.test")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(validDto()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email");
        verify(employeeRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void create_debeLanzarNotFoundSiElDepartamentoNoExiste() {
        when(hasher.hash(any())).thenReturn("hash");
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(validDto()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_debeLanzarNotFoundSiAlgunCargoNoExiste() {
        when(hasher.hash(any())).thenReturn("hash");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department(1L)));
        when(positionRepository.findByIdIn(any())).thenReturn(List.of());   // pidió [1] y no hay ninguno

        assertThatThrownBy(() -> employeeService.createEmployee(validDto()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(employeeRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void create_debeLanzarBadRequestSiElDominioNoEsElDeLaEmpresa() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setEmail("ana@gmail.com");

        assertThatThrownBy(() -> employeeService.createEmployee(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("grh.test");
        verify(employeeRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    // ===================== DEACTIVATE / ACTIVATE =====================

    @Test
    void deactivate_debeDesactivarEmpleadoYSuUsuario() {
        Usuario usuario = new Usuario();
        usuario.setActive(true);
        Employee employee = employee(1L, "Ana", null);
        employee.setActive(true);
        employee.setUsuario(usuario);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.deactivate(1L);

        assertThat(employee.isActive()).isFalse();
        assertThat(usuario.isActive()).isFalse();      // el acceso también queda bloqueado
        verify(employeeRepository, times(1)).save(any());
    }

    @Test
    void activate_debeActivarEmpleadoYSuUsuario() {
        Usuario usuario = new Usuario();
        usuario.setActive(false);
        Employee employee = employee(1L, "Ana", null);
        employee.setActive(false);
        employee.setUsuario(usuario);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.activate(1L);

        assertThat(employee.isActive()).isTrue();
        assertThat(usuario.isActive()).isTrue();
        verify(employeeRepository, times(1)).save(any());
    }

    @Test
    void deactivate_noFallaSiElEmpleadoNoTieneUsuario() {
        Employee employee = employee(1L, "Ana", null);
        employee.setActive(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.deactivate(1L);

        assertThat(employee.isActive()).isFalse();
    }

    @Test
    void deactivate_yActivate_lanzanNotFoundSiNoExiste() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deactivate(99L))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("99");
        assertThatThrownBy(() -> employeeService.activate(99L))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("99");
        verify(employeeRepository, never()).save(any());
    }

    // ===================== UPDATE =====================

    @Test
    void updateEmployee_debeActualizarTodosLosCampos() {
        // Valores de partida distintos a los del DTO en todos los campos
        Employee employee = employee(1L, "Antiguo", department(9L));
        employee.setFirstSurname("Viejo");
        employee.setSecondSurname("Viejo2");
        employee.setPhoneNumber("600000000");
        employee.setAddress("Calle vieja");
        employee.setEmergencyContact("Contacto viejo");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department(1L)));
        when(positionRepository.findByIdIn(any())).thenReturn(List.of(position(1L)));
        when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponseDTO result = employeeService.updateEmployee(1L, updateDto());

        assertThat(result.getName()).isEqualTo("Juan Actualizado");
        assertThat(result.getFirstSurname()).isEqualTo("Nuevo");
        assertThat(result.getSecondSurname()).isEqualTo("López");
        assertThat(result.getPhoneNumber()).isEqualTo("611111111");
        assertThat(result.getAddress()).isEqualTo("Calle Nueva 1");
        assertThat(result.getEmergencyContact()).isEqualTo("Contacto nuevo");
        assertThat(result.getDepartment().getId()).isEqualTo(1L);
        assertThat(result.getPositions()).hasSize(1);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void updateEmployee_lanzaNotFoundSiElEmpleadoNoExiste() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, updateDto()))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("99");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updateEmployee_lanzaNotFoundSiElDepartamentoNoExiste() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(1L, "Ana", null)));
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, updateDto()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updateEmployee_lanzaNotFoundSiAlgunCargoNoExiste() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(1L, "Ana", null)));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department(1L)));
        when(positionRepository.findByIdIn(any())).thenReturn(List.of());

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, updateDto()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(employeeRepository, never()).save(any());
    }

    // ===================== UPDATE ME =====================

    @Test
    void updateMe_soloCambiaLosCamposRecibidos() {
        Employee employee = employee(1L, "Ana", department(1L));
        employee.setEmail("usuario@grh.test");
        employee.setAddress("Antigua");
        employee.setPhoneNumber("123456789");
        employee.setEmergencyContact("Contacto");
        loginWithProfile("usuario@grh.test", ApplicationUserRole.EMPLOYEE, employee);
        when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmployeeProfileDTO dto = new EmployeeProfileDTO();
        dto.setAddress("Nueva");                       // teléfono y contacto quedan a null

        EmployeeResponseDTO result = employeeService.updateMe(dto);

        assertThat(result.getAddress()).isEqualTo("Nueva");
        assertThat(result.getPhoneNumber()).isEqualTo("123456789");          // intacto
        assertThat(result.getEmergencyContact()).isEqualTo("Contacto");      // intacto
        assertThat(result.getEmail()).isEqualTo("usuario@grh.test");         // el email no se toca
        verify(employeeRepository, times(1)).save(any());
    }

    @Test
    void updateMe_conCadenaVaciaBorraElContactoDeEmergencia() {
        Employee employee = employee(1L, "Ana", department(1L));
        employee.setEmergencyContact("Contacto antiguo");
        loginWithProfile("usuario@grh.test", ApplicationUserRole.EMPLOYEE, employee);
        when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmployeeProfileDTO dto = new EmployeeProfileDTO();
        dto.setEmergencyContact("");

        assertThat(employeeService.updateMe(dto).getEmergencyContact()).isNull();
    }

    // ===================== GET BY ID =====================

    @Test
    void getById_ADMIN_devuelveCualquierEmpleado() {
        loginWithProfile("admin@grh.test", ApplicationUserRole.ADMIN, null);
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee(5L, "Ana", department(2L))));

        EmployeeResponseDTO result = employeeService.getById(5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Ana");
        verify(employeeRepository, never()).findByUsuario(any());   // el admin no necesita ficha
    }

    @Test
    void getById_MANAGER_devuelveEmpleadoDeSuDepartamento() {
        loginWithProfile("manager@grh.test", ApplicationUserRole.MANAGER, employee(1L, "Pedro", department(1L)));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee(5L, "Ana", department(1L))));

        assertThat(employeeService.getById(5L).getName()).isEqualTo("Ana");
    }

    @Test
    void getById_MANAGER_lanzaForbiddenSiEsDeOtroDepartamento() {
        loginWithProfile("manager@grh.test", ApplicationUserRole.MANAGER, employee(1L, "Pedro", department(1L)));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee(5L, "Ana", department(2L))));

        assertThatThrownBy(() -> employeeService.getById(5L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getById_MANAGER_sinDepartamento_lanzaForbiddenYNoNPE() {
        loginWithProfile("manager@grh.test", ApplicationUserRole.MANAGER, employee(1L, "Pedro", null));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee(5L, "Ana", department(1L))));

        assertThatThrownBy(() -> employeeService.getById(5L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getById_lanzaNotFoundSiNoExiste() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("99");
    }

    @Test
    void getById_EMPLOYEE_puedeVerseASiMismo() {
        Employee me = employee(5L, "Ana", department(1L));
        loginWithProfile("ana@grh.test", ApplicationUserRole.EMPLOYEE, me);
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(me));

        assertThat(employeeService.getById(5L).getId()).isEqualTo(5L);
    }

    @Test
    void getById_EMPLOYEE_noPuedeVerAOtro() {
        loginWithProfile("ana@grh.test", ApplicationUserRole.EMPLOYEE, employee(5L, "Ana", department(1L)));
        when(employeeRepository.findById(6L)).thenReturn(Optional.of(employee(6L, "Luis", department(1L))));

        assertThatThrownBy(() -> employeeService.getById(6L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    // ===================== GET ALL / OWN PROFILE =====================

    @Test
    void getAll_devuelveTodosLosEmpleados() {
        Department dept = department(1L);
        when(employeeRepository.findAll()).thenReturn(List.of(
                employee(1L, "Ana", dept), employee(2L, "Luis", dept)));

        assertThat(employeeService.getAll())
                .extracting(EmployeeResponseDTO::getName)
                .containsExactly("Ana", "Luis");
    }

    @Test
    void getOwnProfile_devuelvePerfil() {
        loginWithProfile("ana@grh.test", ApplicationUserRole.EMPLOYEE, employee(3L, "Ana", department(1L)));

        EmployeeResponseDTO result = employeeService.getOwnProfile();

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("Ana");
    }
}