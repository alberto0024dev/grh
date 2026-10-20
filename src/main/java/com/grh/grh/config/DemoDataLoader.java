package com.grh.grh.config;

import com.grh.grh.contract.ContractRequestDTO;
import com.grh.grh.contract.ContractService;
import com.grh.grh.contract.ContractType;
import com.grh.grh.contract.WorkPlace;
import com.grh.grh.contract.WorkSchedule;
import com.grh.grh.department.DepartmentRequestDTO;
import com.grh.grh.department.DepartmentService;
import com.grh.grh.department.Status;
import com.grh.grh.employee.EmployeeRequestDTO;
import com.grh.grh.employee.EmployeeResponseDTO;
import com.grh.grh.employee.EmployeeService;
import com.grh.grh.position.JobCategory;
import com.grh.grh.position.PositionRequestDTO;
import com.grh.grh.position.PositionService;
import com.grh.grh.usuario.ApplicationUserRole;
import com.grh.grh.usuario.Usuario;
import com.grh.grh.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoDataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PositionService positionService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final ContractService contractService;

    @Value("${app.email-domain}")
    private String domain;

    @Value("${app.demo-password}")
    private String demoPassword;

    @Override
    @Transactional
    public void run(String... args) {
        String adminEmail = "admin@" + domain;
        if (usuarioRepository.existsByEmail(adminEmail)) return;   // ya hay datos: no duplicar

        // 1. Admin (no tiene ficha de empleado)
        usuarioRepository.save(Usuario.create(adminEmail, demoPassword, ApplicationUserRole.ADMIN, passwordEncoder));

        // 2. Tres cargos
        Long dev = position("Desarrollador", JobCategory.SENIOR);
        Long analyst = position("Analista", JobCategory.JUNIOR);
        Long lead = position("Responsable de equipo", JobCategory.LEAD);

        // 3. Dos departamentos (sin manager todavía)
        DepartmentRequestDTO techDto = departmentDto("Tecnología", "Edificio A", 2, "320000");
        DepartmentRequestDTO opsDto = departmentDto("Operaciones", "Edificio B", 1, "180000");
        Long tech = departmentService.create(techDto).getId();
        Long ops = departmentService.create(opsDto).getId();

        // 4. Tecnología: 5 empleados. Presupuesto 320.000 frente a 214.000 de masa salarial (sobra)
        Long pedro = employee("Pedro", "Martínez", "12345678Z", "pedro.martinez", "100000000001",
                ApplicationUserRole.MANAGER, tech, lead, LocalDate.of(1984, 3, 12));
        Long ana = employee("Ana", "García", "00000000T", "ana.garcia", "100000000002",
                ApplicationUserRole.EMPLOYEE, tech, dev, LocalDate.of(1994, 7, 21));
        Long luis = employee("Luis", "Pérez", "11111111H", "luis.perez", "100000000003",
                ApplicationUserRole.EMPLOYEE, tech, analyst, LocalDate.of(1999, 11, 5));
        Long marta = employee("Marta", "Ruiz", "87654321X", "marta.ruiz", "100000000004",
                ApplicationUserRole.EMPLOYEE, tech, dev, LocalDate.of(1990, 2, 17));
        Long diego = employee("Diego", "Sanz", "10000000Z", "diego.sanz", "100000000005",
                ApplicationUserRole.EMPLOYEE, tech, analyst, LocalDate.of(2002, 9, 30));

        contract(pedro, ContractType.PERMANENT, 48, null, "62000", WorkPlace.HYBRID);
        contract(ana, ContractType.PERMANENT, 30, null, "48000", WorkPlace.REMOTE);
        contract(luis, ContractType.TEMPORARY, 8, daysFromNow(20), "34000", WorkPlace.OFFICE);
        contract(marta, ContractType.PERMANENT, 60, null, "52000", WorkPlace.HYBRID);
        contract(diego, ContractType.INTERNSHIP, 5, daysFromNow(12), "18000", WorkPlace.OFFICE);

        // 5. Operaciones: 5 empleados, uno dado de baja.
        //    Presupuesto 180.000 frente a 196.000 de masa salarial activa (se pasa: saldrá en rojo)
        Long laura = employee("Laura", "Gómez", "20000000M", "laura.gomez", "100000000006",
                ApplicationUserRole.MANAGER, ops, lead, LocalDate.of(1982, 5, 8));
        Long carlos = employee("Carlos", "Díaz", "30000000L", "carlos.diaz", "100000000007",
                ApplicationUserRole.EMPLOYEE, ops, analyst, LocalDate.of(1996, 12, 2));
        Long elena = employee("Elena", "Torres", "40000000X", "elena.torres", "100000000008",
                ApplicationUserRole.EMPLOYEE, ops, analyst, LocalDate.of(1998, 4, 14));
        Long pablo = employee("Pablo", "Navarro", "50000000R", "pablo.navarro", "100000000009",
                ApplicationUserRole.EMPLOYEE, ops, dev, LocalDate.of(1992, 8, 19));
        Long sofia = employee("Sofía", "Molina", "60000000S", "sofia.molina", "100000000010",
                ApplicationUserRole.EMPLOYEE, ops, dev, LocalDate.of(2001, 1, 25));

        contract(laura, ContractType.PERMANENT, 72, null, "58000", WorkPlace.OFFICE);
        contract(carlos, ContractType.PERMANENT, 20, null, "36000", WorkPlace.OFFICE);
        contract(elena, ContractType.TEMPORARY, 6, daysFromNow(28), "33000", WorkPlace.HYBRID);
        contract(pablo, ContractType.PERMANENT, 40, null, "64000", WorkPlace.REMOTE);
        contract(sofia, ContractType.TEMPORARY, 3, daysFromNow(75), "41000", WorkPlace.REMOTE);

        employeeService.deactivate(carlos);   // empleado de baja: sale en "inactivos" y no cuenta en el resto

        // 6. Managers asignados al final, cuando ya existen
        techDto.setManagerId(pedro);
        departmentService.update(tech, techDto);
        opsDto.setManagerId(laura);
        departmentService.update(ops, opsDto);
    }

    private LocalDate daysFromNow(int days) {
        return LocalDate.now().plusDays(days);
    }

    private Long position(String name, JobCategory category) {
        PositionRequestDTO p = new PositionRequestDTO();
        p.setName(name);
        p.setJobCategory(category);
        return positionService.create(p).getId();
    }

    private DepartmentRequestDTO departmentDto(String name, String building, int floor, String budget) {
        DepartmentRequestDTO d = new DepartmentRequestDTO();
        d.setName(name);
        d.setBuilding(building);
        d.setFloor(floor);
        d.setAddress("Calle Mayor 1, Madrid");
        d.setStatus(Status.AVAILABLE);
        d.setContactPhone("912345678");
        d.setAnnualBudget(new BigDecimal(budget));
        return d;
    }

    private Long employee(String name, String surname, String dni, String user, String ss,
                          ApplicationUserRole role, Long deptId, Long positionId, LocalDate birth) {
        EmployeeRequestDTO e = new EmployeeRequestDTO();
        e.setName(name);
        e.setFirstSurname(surname);
        e.setIdNumber(dni);
        e.setAddress("Calle Demo 1");
        e.setPhoneNumber("600000000");
        e.setEmail(user + "@" + domain);
        e.setBirthDate(birth);
        e.setSocialSecurityNumber(ss);
        e.setRole(role);
        e.setDepartmentId(deptId);
        e.setPositionIds(List.of(positionId));
        EmployeeResponseDTO created = employeeService.createEmployee(e);

        // Sustituimos la contraseña temporal aleatoria por la de demo
        Usuario u = usuarioRepository.findByEmail(e.getEmail().toLowerCase()).orElseThrow();
        u.setPassword(passwordEncoder.encode(demoPassword));
        usuarioRepository.save(u);
        return created.getId();
    }

    private void contract(Long employeeId, ContractType type, int monthsAgo, LocalDate end,
                          String salary, WorkPlace place) {
        ContractRequestDTO c = new ContractRequestDTO();
        c.setEmployeeId(employeeId);
        c.setContractType(type);
        c.setCommencementDate(LocalDate.now().minusMonths(monthsAgo));
        c.setExpiryDate(end);
        c.setVacationDays(22);
        c.setSalary(new BigDecimal(salary));
        c.setWorkPlace(place);
        WorkSchedule w = new WorkSchedule();
        w.setWeeklyHours(type == ContractType.INTERNSHIP ? 30 : 40);
        w.setStartTime(LocalTime.of(9, 0));
        w.setEndTime(LocalTime.of(18, 0));
        w.setDaysOff(2);
        c.setWorkSchedule(w);
        contractService.create(c);
    }
}