package com.grh.grh.contract;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @InjectMocks
    private ContractService contractService;


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

    @Test
    void create_debeCrearContratoYEnlazarloAlEmpleado() {
        Employee employee = new Employee();
        employee.setId(1L);

        WorkSchedule schedule = new WorkSchedule();
        schedule.setWeeklyHours(40);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        schedule.setDaysOff(2);

        ContractRequestDTO dto = new ContractRequestDTO();
        dto.setEmployeeId(1L);
        dto.setContractType(ContractType.PERMANENT);
        dto.setCommencementDate(LocalDate.now());
        dto.setSalary(BigDecimal.valueOf(30000));
        dto.setWorkPlace(WorkPlace.OFFICE);
        dto.setVacationDays(22);
        dto.setWorkSchedule(schedule);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        ContractResponseDTO result = contractService.create(dto);

        assertThat(result.getContractType()).isEqualTo(ContractType.PERMANENT);
        assertThat(result.getSalary()).isEqualByComparingTo("30000");
        assertThat(employee.getContract()).isNotNull();
        verify(contractRepository, times(1)).save(any(Contract.class));
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void create_debeLanzarExcepcionCuandoEmpleadoNoExiste() {
        ContractRequestDTO dto = new ContractRequestDTO();
        dto.setEmployeeId(99L);

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_debeActualizarTodosLosCamposDelContrato() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setContractType(ContractType.TEMPORARY);
        contract.setSalary(BigDecimal.valueOf(20000));

        WorkSchedule schedule = new WorkSchedule();
        schedule.setWeeklyHours(35);
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(16, 0));
        schedule.setDaysOff(2);

        ContractUpdateDTO dto = new ContractUpdateDTO();
        dto.setContractType(ContractType.PERMANENT);
        dto.setSalary(BigDecimal.valueOf(35000));
        dto.setCommencementDate(LocalDate.of(2026, 1, 1));
        dto.setExpiryDate(null);
        dto.setWorkPlace(WorkPlace.REMOTE);
        dto.setVacationDays(25);
        dto.setWorkSchedule(schedule);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

        ContractResponseDTO result = contractService.update(1L, dto);

        assertThat(result.getContractType()).isEqualTo(ContractType.PERMANENT);
        assertThat(result.getSalary()).isEqualByComparingTo("35000");
        assertThat(result.getVacationDays()).isEqualTo(25);
        assertThat(result.getWorkPlace()).isEqualTo(WorkPlace.REMOTE);
        assertThat(result.getExpiryDate()).isNull();
        assertThat(result.getWorkSchedule().getWeeklyHours()).isEqualTo(35);
        verify(contractRepository, times(1)).save(contract);
    }

    @Test
    void update_debeLanzarExcepcionCuandoNoExiste() {
        when(contractRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.update(99L, new ContractUpdateDTO()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(contractRepository, never()).save(any());
    }

    @Test
    void delete_debeDesvincularAlEmpleadoYBorrarElContrato() {
        Employee employee = new Employee();
        employee.setId(1L);
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setEmployee(employee);
        employee.setContract(contract);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        contractService.delete(1L);

        assertThat(employee.getContract()).isNull();
        verify(employeeRepository, times(1)).save(employee);
        verify(contractRepository, times(1)).delete(contract);
    }

    @Test
    void delete_debeBorrarContratoSinEmpleado() {
        Contract contract = new Contract();
        contract.setId(1L);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        contractService.delete(1L);

        verify(employeeRepository, never()).save(any());
        verify(contractRepository, times(1)).delete(contract);
    }

    @Test
    void delete_debeLanzarExcepcionCuandoNoExiste() {
        when(contractRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(contractRepository, never()).delete(any());

    }

    @Test
    void getById_Admin_debeRetornarContrato() {
        Usuario admin = loginAs("admin@grh.test", ApplicationUserRole.ADMIN);
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setContractType(ContractType.PERMANENT);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(usuarioRepository.findByEmail("admin@grh.test")).thenReturn(Optional.of(admin));

        ContractResponseDTO result = contractService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContractType()).isEqualTo(ContractType.PERMANENT);
        verify(employeeRepository, never()).findByUsuario(any());
    }

    @Test
    void getById_Employee_debeRetornarSuPropioContrato() {
        Usuario usuario = loginAs("empleado@grh.test", ApplicationUserRole.EMPLOYEE);
        Employee employee = new Employee();
        employee.setId(1L);
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setContractType(ContractType.PERMANENT);
        contract.setEmployee(employee);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(usuarioRepository.findByEmail("empleado@grh.test")).thenReturn(Optional.of(usuario));
        when(employeeRepository.findByUsuario(usuario)).thenReturn(Optional.of(employee));

        ContractResponseDTO result = contractService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_Employee_debeLanzarForbiddenSiElContratoEsDeOtro() {
        Usuario usuario = loginAs("empleado@grh.test", ApplicationUserRole.EMPLOYEE);
        Employee me = new Employee();
        me.setId(1L);
        Employee owner = new Employee();
        owner.setId(2L);
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setEmployee(owner);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(usuarioRepository.findByEmail("empleado@grh.test")).thenReturn(Optional.of(usuario));
        when(employeeRepository.findByUsuario(usuario)).thenReturn(Optional.of(me));

        assertThatThrownBy(() -> contractService.getById(1L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getById_debeLanzarForbiddenSiElContratoNoTieneEmpleado() {
        Usuario usuario = loginAs("empleado@grh.test", ApplicationUserRole.EMPLOYEE);
        Employee me = new Employee();
        me.setId(1L);
        Contract contract = new Contract();
        contract.setId(1L);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(usuarioRepository.findByEmail("empleado@grh.test")).thenReturn(Optional.of(usuario));
        when(employeeRepository.findByUsuario(usuario)).thenReturn(Optional.of(me));

        assertThatThrownBy(() -> contractService.getById(1L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getAll_debeDevolverTodosLosContratos() {
        Contract contract1 = new Contract();
        contract1.setId(1L);
        Contract contract2 = new Contract();
        contract2.setId(2L);

        when(contractRepository.findAll()).thenReturn(List.of(contract1, contract2));

        List<ContractResponseDTO> result = contractService.getAll();

        assertThat(result).hasSize(2);
        verify(contractRepository, times(1)).findAll();
        assertThat(result).extracting(ContractResponseDTO::getId).containsExactly(1L, 2L);
    }
    @Test
    void getById_Manager_debeRetornarContratoDeSuDepartamento() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        Department department = new Department();
        department.setId(1L);
        Employee owner = new Employee();
        owner.setId(10L);
        owner.setDepartment(department);
        Employee managerEmployee = new Employee();
        managerEmployee.setId(20L);
        managerEmployee.setDepartment(department);
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setEmployee(owner);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(managerEmployee));

        assertThat(contractService.getById(1L).getId()).isEqualTo(1L);
    }

    @Test
    void getById_Manager_debeLanzarExcepcionSiEsDeOtroDepartamento() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);

        Department ownDepartment = new Department();
        ownDepartment.setId(1L);
        Department otherDepartment = new Department();
        otherDepartment.setId(2L);

        Employee owner = new Employee();
        owner.setId(10L);
        owner.setDepartment(otherDepartment);

        Employee managerEmployee = new Employee();
        managerEmployee.setId(20L);
        managerEmployee.setDepartment(ownDepartment);

        Contract contract = new Contract();
        contract.setId(1L);
        contract.setEmployee(owner);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(managerEmployee));

        assertThatThrownBy(() -> contractService.getById(1L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getById_debeLanzarExcepcionSiElContratoNoTieneEmpleado() {
        Usuario usuario = loginAs("empleado@grh.test", ApplicationUserRole.EMPLOYEE);
        Employee employeeLogged = new Employee();
        employeeLogged.setId(2L);
        Contract contract = new Contract();
        contract.setId(1L);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(usuarioRepository.findByEmail("empleado@grh.test")).thenReturn(Optional.of(usuario));
        when(employeeRepository.findByUsuario(usuario)).thenReturn(Optional.of(employeeLogged));

        assertThatThrownBy(() -> contractService.getById(1L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void create_debeLanzarConflictCuandoElEmpleadoYaTieneContrato() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setContract(new Contract());

        ContractRequestDTO dto = new ContractRequestDTO();
        dto.setEmployeeId(1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> contractService.create(dto))
                .isInstanceOf(ConflictException.class);
        verify(contractRepository, never()).save(any());
        verify(employeeRepository, never()).save(any());
    }

    private Employee emp(Long id, Department d) {
        Employee e = new Employee();
        e.setId(id);
        e.setDepartment(d);
        return e;
    }

    private Department dept(Long id) {
        Department d = new Department();
        d.setId(id);
        return d;
    }

    @Test
    void getByEmployeeId_Admin_devuelveContrato() {
        Usuario admin = loginAs("admin@grh.test", ApplicationUserRole.ADMIN);
        Employee target = emp(5L, dept(1L));
        Contract c = new Contract();
        c.setId(9L);

        when(usuarioRepository.findByEmail("admin@grh.test")).thenReturn(Optional.of(admin));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(target));
        when(contractRepository.findByEmployee(target)).thenReturn(Optional.of(c));

        assertThat(contractService.getByEmployeeId(5L).getId()).isEqualTo(9L);
        verify(employeeRepository, never()).findByUsuario(any());
    }

    @Test
    void getByEmployeeId_Employee_propioContrato() {
        Usuario u = loginAs("ana@grh.test", ApplicationUserRole.EMPLOYEE);
        Employee me = emp(5L, dept(1L));
        Contract c = new Contract();
        c.setId(9L);

        when(usuarioRepository.findByEmail("ana@grh.test")).thenReturn(Optional.of(u));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(me));
        when(employeeRepository.findByUsuario(u)).thenReturn(Optional.of(me));
        when(contractRepository.findByEmployee(me)).thenReturn(Optional.of(c));

        assertThat(contractService.getByEmployeeId(5L).getId()).isEqualTo(9L);
    }

    @Test
    void getByEmployeeId_Employee_ajeno_lanzaForbidden() {
        Usuario u = loginAs("ana@grh.test", ApplicationUserRole.EMPLOYEE);
        Employee me = emp(5L, dept(1L));
        Employee other = emp(6L, dept(1L));

        when(usuarioRepository.findByEmail("ana@grh.test")).thenReturn(Optional.of(u));
        when(employeeRepository.findById(6L)).thenReturn(Optional.of(other));
        when(employeeRepository.findByUsuario(u)).thenReturn(Optional.of(me));

        assertThatThrownBy(() -> contractService.getByEmployeeId(6L))
                .isInstanceOf(ForbiddenAccessException.class);
        verify(contractRepository, never()).findByEmployee(any());
    }

    @Test
    void getByEmployeeId_Manager_mismoDepartamento_devuelveContrato() {
        Usuario u = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        Department d = dept(1L);
        Employee manager = emp(1L, d);
        Employee target = emp(5L, d);
        Contract c = new Contract();
        c.setId(9L);

        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(u));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(target));
        when(employeeRepository.findByUsuario(u)).thenReturn(Optional.of(manager));
        when(contractRepository.findByEmployee(target)).thenReturn(Optional.of(c));

        assertThat(contractService.getByEmployeeId(5L).getId()).isEqualTo(9L);
    }

    @Test
    void getByEmployeeId_Manager_otroDepartamento_lanzaForbidden() {
        Usuario u = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        Employee manager = emp(1L, dept(1L));
        Employee target = emp(5L, dept(2L));

        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(u));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(target));
        when(employeeRepository.findByUsuario(u)).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> contractService.getByEmployeeId(5L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getByEmployeeId_Manager_sinDepartamento_lanzaForbiddenYNoNPE() {
        Usuario u = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        Employee manager = emp(1L, null);
        Employee target = emp(5L, dept(1L));

        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(u));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(target));
        when(employeeRepository.findByUsuario(u)).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> contractService.getByEmployeeId(5L))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getByEmployeeId_EmpleadoSinContrato_lanzaNotFound() {
        Usuario admin = loginAs("admin@grh.test", ApplicationUserRole.ADMIN);
        Employee target = emp(5L, dept(1L));

        when(usuarioRepository.findByEmail("admin@grh.test")).thenReturn(Optional.of(admin));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(target));
        when(contractRepository.findByEmployee(target)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.getByEmployeeId(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByEmployeeId_EmpleadoNoExiste_lanzaNotFound() {
        Usuario admin = loginAs("admin@grh.test", ApplicationUserRole.ADMIN);

        when(usuarioRepository.findByEmail("admin@grh.test")).thenReturn(Optional.of(admin));
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.getByEmployeeId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}