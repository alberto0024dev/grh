package com.grh.grh.reports;

import com.grh.grh.contract.Contract;
import com.grh.grh.contract.ContractType;
import com.grh.grh.contract.WorkPlace;
import com.grh.grh.department.Department;
import com.grh.grh.department.DepartmentRepository;
import com.grh.grh.employee.Employee;
import com.grh.grh.employee.EmployeeRepository;
import com.grh.grh.exception.ForbiddenAccessException;
import com.grh.grh.exception.ResourceNotFoundException;
import com.grh.grh.position.JobCategory;
import com.grh.grh.position.Position;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportsServiceTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private ReportsService reportsService;

    @BeforeEach
    void loginComoAdmin() {
        Usuario admin = loginAs("admin@grh.test", ApplicationUserRole.ADMIN);
        lenient().when(usuarioRepository.findByEmail("admin@grh.test")).thenReturn(Optional.of(admin));
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    // ===================== AUXILIARES =====================

    private Usuario loginAs(String email, ApplicationUserRole role) {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setRole(role);
        u.setActive(true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities()));
        return u;
    }

    // Departamento 1 con esos empleados, ya registrado en el mock
    private Department departmentWith(Employee... employees) {
        Department d = new Department();
        d.setId(1L);
        d.setEmployees(List.of(employees));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(d));
        return d;
    }

    private Employee employee(boolean active, Contract contract) {
        Employee e = new Employee();
        e.setActive(active);
        e.setContract(contract);
        e.setPositions(List.of());
        return e;
    }

    private Contract contract(ContractType type, String salary) {
        Contract c = new Contract();
        c.setContractType(type);
        c.setSalary(new BigDecimal(salary));
        return c;
    }

    private Contract contractWithWorkPlace(WorkPlace place) {
        Contract c = contract(ContractType.PERMANENT, "30000");
        c.setWorkPlace(place);
        return c;
    }

    private Contract expiring(ContractType type, LocalDate expiry) {
        Contract c = contract(type, "30000");
        c.setExpiryDate(expiry);
        return c;
    }

    private Employee named(String name, Contract c) {
        Employee e = employee(true, c);
        e.setName(name);
        e.setFirstSurname("Apellido");
        return e;
    }

    private Position position(Long id, String name) {
        return new Position(id, name, JobCategory.SENIOR, List.of());
    }

    // MANAGER cuyo departamento es el 1
    private void loginAsManagerOfDepartment(Department own) {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        Employee me = new Employee();
        me.setDepartment(own);
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(me));
    }

    private Department dept(Long id) {
        Department d = new Department();
        d.setId(id);
        return d;
    }

    // ===================== SALARIO MEDIO =====================

    @Test
    void averageSalary_calculaLaMediaSoloDeActivosConContrato() {
        departmentWith(
                employee(true, contract(ContractType.PERMANENT, "30000")),
                employee(true, contract(ContractType.PERMANENT, "50000")),
                employee(false, contract(ContractType.PERMANENT, "900000")),   // inactivo: no cuenta
                employee(true, null));                                          // sin contrato: no cuenta

        assertThat(reportsService.averageSalaryPerDepartment(1L)).isCloseTo(40000.0, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void averageSalary_departamentoSinContratos_devuelveCero() {
        departmentWith(employee(true, null));

        assertThat(reportsService.averageSalaryPerDepartment(1L)).isEqualTo(0.0);
    }

    // ===================== EMPLEADOS ACTIVOS / POR DEPARTAMENTO =====================

    @Test
    void employeesPerDepartment_cuentaSoloActivos() {
        departmentWith(employee(true, null), employee(true, null), employee(false, null));

        assertThat(reportsService.employeesPerDepartment(1L)).isEqualTo(2L);
    }

    @Test
    void activeEmployees_separaActivosEInactivos() {
        departmentWith(employee(true, null), employee(true, null), employee(false, null));

        Map<String, Long> result = reportsService.activeEmployees(1L);

        assertThat(result).containsEntry("Activos", 2L).containsEntry("Inactivos", 1L);
    }

    @Test
    void activeEmployees_departamentoVacio_devuelveMapaVacio() {
        departmentWith();

        // Sin empleados no hay claves: por eso el frontend usa "?? 0"
        assertThat(reportsService.activeEmployees(1L)).isEmpty();
    }

    // ===================== POR TIPO DE CONTRATO =====================

    @Test
    void employeesPerContractType_agrupaIgnorandoInactivosYSinContrato() {
        departmentWith(
                employee(true, contract(ContractType.PERMANENT, "1")),
                employee(true, contract(ContractType.PERMANENT, "1")),
                employee(true, contract(ContractType.INTERNSHIP, "1")),
                employee(true, contract(ContractType.TEMPORARY, "1")),
                employee(false, contract(ContractType.TEMPORARY, "1")),   // inactivo
                employee(true, null));                                    // sin contrato

        Map<String, Long> result = reportsService.employeesPerContractType(1L);

        assertThat(result)
                .containsEntry("PERMANENT", 2L)
                .containsEntry("INTERNSHIP", 1L)
                .containsEntry("TEMPORARY", 1L)
                .hasSize(3);
    }

    // ===================== CONTRATOS QUE VENCEN =====================

    @Test
    void expiringContracts_devuelveSoloLosQueVencenEnElRango() {
        LocalDate today = LocalDate.now();
        Employee ana = named("Ana", expiring(ContractType.TEMPORARY, today.plusDays(5)));
        Employee luis = named("Luis", expiring(ContractType.INTERNSHIP, today.plusDays(29)));
        Employee fueraDeRango = named("Lejos", expiring(ContractType.TEMPORARY, today.plusDays(90)));
        Employee sinFecha = named("SinFecha", expiring(ContractType.TEMPORARY, null));
        Employee permanente = named("Fijo", contract(ContractType.PERMANENT, "1"));
        Employee inactivo = named("Baja", expiring(ContractType.TEMPORARY, today.plusDays(3)));
        inactivo.setActive(false);
        departmentWith(ana, luis, fueraDeRango, sinFecha, permanente, inactivo);

        List<ExpiringContractDTO> result = reportsService.expiringContracts(1L, 30);

        assertThat(result).extracting(ExpiringContractDTO::getEmployeeName).containsExactly("Ana", "Luis");
        assertThat(result.getFirst().getDaysLeft()).isEqualTo(5L);
        assertThat(result.getFirst().getContractType()).isEqualTo(ContractType.TEMPORARY);
    }

    @Test
    void expiringContracts_ordenaPorFechaDeFin() {
        LocalDate today = LocalDate.now();
        departmentWith(
                named("Lejano", expiring(ContractType.TEMPORARY, today.plusDays(20))),
                named("Cercano", expiring(ContractType.INTERNSHIP, today.plusDays(2))),
                named("Medio", expiring(ContractType.TEMPORARY, today.plusDays(10))));

        assertThat(reportsService.expiringContracts(1L, 30))
                .extracting(ExpiringContractDTO::getEmployeeName)
                .containsExactly("Cercano", "Medio", "Lejano");
    }

    @Test
    void expiringContracts_loQueVenceHoyOExactamenteEnElLimiteQuedaFuera() {
        // Documenta el comportamiento actual (isAfter / isBefore, ambos estrictos).
        // Si decides que "vence hoy" debe salir, cambia el servicio y este test.
        LocalDate today = LocalDate.now();
        departmentWith(
                named("Hoy", expiring(ContractType.TEMPORARY, today)),
                named("Limite", expiring(ContractType.TEMPORARY, today.plusDays(30))));

        assertThat(reportsService.expiringContracts(1L, 30)).isEmpty();
    }

    // ===================== ANTIGÜEDAD Y EDAD =====================

    @Test
    void averageTenure_calculaMesesIgnorandoInactivosYSinContrato() {
        Contract hace12 = contract(ContractType.PERMANENT, "1");
        hace12.setCommencementDate(LocalDate.now().minusMonths(12));
        Contract hace6 = contract(ContractType.PERMANENT, "1");
        hace6.setCommencementDate(LocalDate.now().minusMonths(6));
        Contract hace60 = contract(ContractType.PERMANENT, "1");
        hace60.setCommencementDate(LocalDate.now().minusMonths(60));
        departmentWith(
                employee(true, hace12),
                employee(true, hace6),
                employee(false, hace60),     // inactivo: no cuenta
                employee(true, null));       // sin contrato: no cuenta

        assertThat(reportsService.averageTenure(1L)).isEqualTo(9.0);
    }

    @Test
    void averageTenure_departamentoVacio_devuelveCero() {
        departmentWith();

        assertThat(reportsService.averageTenure(1L)).isEqualTo(0.0);
    }

    @Test
    void averageAge_calculaAniosIgnorandoInactivos() {
        Employee a = employee(true, null);
        a.setBirthDate(LocalDate.now().minusYears(30));
        Employee b = employee(true, null);
        b.setBirthDate(LocalDate.now().minusYears(40));
        Employee inactivo = employee(false, null);
        inactivo.setBirthDate(LocalDate.now().minusYears(60));
        departmentWith(a, b, inactivo);

        assertThat(reportsService.averageAge(1L)).isEqualTo(35.0);
    }

    @Test
    void averageAge_departamentoVacio_devuelveCero() {
        departmentWith();

        assertThat(reportsService.averageAge(1L)).isEqualTo(0.0);
    }

    // ===================== POR CARGO Y LUGAR DE TRABAJO =====================

    @Test
    void employeesByPosition_cuentaCadaCargoIgnorandoInactivosYSinCargos() {
        Position dev = position(1L, "Desarrollador");
        Position ing = position(2L, "Ingeniero");
        Employee uno = employee(true, null);
        uno.setPositions(List.of(dev));
        Employee dos = employee(true, null);
        dos.setPositions(List.of(ing));
        Employee tres = employee(true, null);
        tres.setPositions(List.of(ing, dev));          // varios cargos: cuenta en cada uno
        Employee inactivo = employee(false, null);
        inactivo.setPositions(List.of(dev));
        Employee sinCargos = employee(true, null);     // positions vacía
        departmentWith(uno, dos, tres, inactivo, sinCargos);

        Map<String, Long> result = reportsService.employeesByPosition(1L);

        assertThat(result).containsEntry("Desarrollador", 2L).containsEntry("Ingeniero", 2L).hasSize(2);
    }

    @Test
    void employeesByWorkplace_agrupaIgnorandoInactivosYSinContrato() {
        departmentWith(
                employee(true, contractWithWorkPlace(WorkPlace.HYBRID)),
                employee(true, contractWithWorkPlace(WorkPlace.OFFICE)),
                employee(true, contractWithWorkPlace(WorkPlace.OFFICE)),
                employee(true, contractWithWorkPlace(WorkPlace.REMOTE)),
                employee(false, contractWithWorkPlace(WorkPlace.REMOTE)),   // inactivo
                employee(true, null));                                      // sin contrato

        Map<String, Long> result = reportsService.employeesByWorkplace(1L);

        assertThat(result)
                .containsEntry("HYBRID", 1L)
                .containsEntry("OFFICE", 2L)
                .containsEntry("REMOTE", 1L)
                .hasSize(3);
    }

    // ===================== RANGO SALARIAL =====================

    @Test
    void salaryRange_calculaMinMaxYMediaIgnorandoInactivosYSinContrato() {
        departmentWith(
                employee(true, contract(ContractType.PERMANENT, "20000")),
                employee(true, contract(ContractType.PERMANENT, "10000")),
                employee(false, contract(ContractType.PERMANENT, "999999")),   // inactivo
                employee(true, null));                                         // sin contrato

        SalaryRangeDTO result = reportsService.salaryRange(1L);

        assertThat(result.getAverage()).isEqualByComparingTo("15000");
        assertThat(result.getMax()).isEqualByComparingTo("20000");
        assertThat(result.getMin()).isEqualByComparingTo("10000");
    }

    @Test
    void salaryRange_departamentoSinContratos_devuelveCeros() {
        departmentWith(employee(true, null));

        SalaryRangeDTO result = reportsService.salaryRange(1L);

        assertThat(result.getAverage()).isEqualByComparingTo("0");
        assertThat(result.getMax()).isEqualByComparingTo("0");
        assertThat(result.getMin()).isEqualByComparingTo("0");
    }

    // ===================== ALTAS DEL AÑO =====================

    @Test
    void newHires_cuentaLosContratosQueEmpiezanEseAnio() {
        int year = LocalDate.now().getYear();
        Contract esteAnio = contract(ContractType.PERMANENT, "1");
        esteAnio.setCommencementDate(LocalDate.of(year, 1, 15));
        Contract anioAnterior = contract(ContractType.PERMANENT, "1");
        anioAnterior.setCommencementDate(LocalDate.of(year - 1, 6, 1));
        departmentWith(employee(true, esteAnio), employee(true, anioAnterior), employee(true, null));

        assertThat(reportsService.newHires(1L, year)).isEqualTo(1L);
    }

    @Test
    void newHires_tambienCuentaAQuienYaEstaDeBaja() {
        // Documenta el comportamiento actual: el servicio no filtra por activo.
        int year = LocalDate.now().getYear();
        Contract c = contract(ContractType.PERMANENT, "1");
        c.setCommencementDate(LocalDate.of(year, 3, 1));
        departmentWith(employee(false, c));

        assertThat(reportsService.newHires(1L, year)).isEqualTo(1L);
    }

    // ===================== PRESUPUESTO =====================

    @Test
    void budgetSituation_calculaMasaSalarialYDiferencia() {
        Department d = departmentWith(
                employee(true, contract(ContractType.PERMANENT, "10000")),
                employee(true, contract(ContractType.PERMANENT, "15000")),
                employee(false, contract(ContractType.PERMANENT, "99999")));   // inactivo: no cuenta
        d.setAnnualBudget(new BigDecimal("900000"));

        Map<String, BigDecimal> result = reportsService.budgetSituation(1L);

        assertThat(result.get("Presupuesto anual del departamento")).isEqualByComparingTo("900000");
        assertThat(result.get("Masa salarial")).isEqualByComparingTo("25000");
        assertThat(result.get("Diferencia")).isEqualByComparingTo("875000");
    }

    @Test
    void budgetSituation_siLaMasaSalarialSuperaElPresupuesto_laDiferenciaEsNegativa() {
        Department d = departmentWith(employee(true, contract(ContractType.PERMANENT, "200000")));
        d.setAnnualBudget(new BigDecimal("150000"));

        assertThat(reportsService.budgetSituation(1L).get("Diferencia")).isEqualByComparingTo("-50000");
    }

    // ===================== ERRORES Y ACCESO POR ROL =====================

    @Test
    void reporte_departamentoInexistente_lanzaNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportsService.averageSalaryPerDepartment(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void manager_conSuPropioDepartamento_puedeVerElReporte() {
        Department own = departmentWith(employee(true, null));
        loginAsManagerOfDepartment(own);

        assertThat(reportsService.employeesPerDepartment(1L)).isEqualTo(1L);
    }

    @Test
    void manager_deOtroDepartamento_lanzaForbiddenEnTodosLosReportes() {
        loginAsManagerOfDepartment(dept(1L));

        assertThatThrownBy(() -> reportsService.averageSalaryPerDepartment(2L)).isInstanceOf(ForbiddenAccessException.class);
        assertThatThrownBy(() -> reportsService.employeesPerDepartment(2L)).isInstanceOf(ForbiddenAccessException.class);
        assertThatThrownBy(() -> reportsService.expiringContracts(2L, 30)).isInstanceOf(ForbiddenAccessException.class);
        assertThatThrownBy(() -> reportsService.salaryRange(2L)).isInstanceOf(ForbiddenAccessException.class);
        assertThatThrownBy(() -> reportsService.budgetSituation(2L)).isInstanceOf(ForbiddenAccessException.class);
        verify(departmentRepository, never()).findById(any());
    }

    @Test
    void manager_sinDepartamento_lanzaForbidden() {
        Usuario manager = loginAs("manager@grh.test", ApplicationUserRole.MANAGER);
        when(usuarioRepository.findByEmail("manager@grh.test")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByUsuario(manager)).thenReturn(Optional.of(new Employee()));

        assertThatThrownBy(() -> reportsService.employeesPerDepartment(1L))
                .isInstanceOf(ForbiddenAccessException.class);
    }
}