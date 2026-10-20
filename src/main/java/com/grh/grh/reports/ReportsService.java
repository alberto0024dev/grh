package com.grh.grh.reports;

import com.grh.grh.contract.Contract;
import com.grh.grh.contract.ContractResponseDTO;
import com.grh.grh.contract.ContractType;
import com.grh.grh.department.Department;
import com.grh.grh.department.DepartmentRepository;
import com.grh.grh.employee.Employee;
import com.grh.grh.employee.EmployeeRepository;
import com.grh.grh.exception.ForbiddenAccessException;
import com.grh.grh.exception.ResourceNotFoundException;
import com.grh.grh.position.Position;
import com.grh.grh.usuario.ApplicationUserRole;
import com.grh.grh.usuario.Usuario;
import com.grh.grh.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final DepartmentRepository departmentRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmployeeRepository employeeRepository;

    private void checkDepartmentAccess(Long departmentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario userLogged = usuarioRepository.findByEmail(email).orElseThrow();

        if (userLogged.getRole() == ApplicationUserRole.ADMIN) return;

        Employee employeeLogged = employeeRepository.findByUsuario(userLogged).orElseThrow();
        if (employeeLogged.getDepartment() == null
                || !employeeLogged.getDepartment().getId().equals(departmentId)) {
            throw new ForbiddenAccessException("No tienes acceso a los reportes de este departamento");
        }
    }


    @Transactional
    public Double averageSalaryPerDepartment(Long id){
        checkDepartmentAccess(id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento con id: "+id+" no encontrado"));
        return department.getEmployees().stream()
                .filter(e->e.getContract()!=null && e.isActive())
                .mapToDouble(e->e.getContract().getSalary().doubleValue())
                .average()
                .orElse(0.0);
    }

    @Transactional
    public Long employeesPerDepartment(Long id){
        checkDepartmentAccess(id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento con id: "+id+" no encontrado"));
        return department.getEmployees().stream()
                .filter(Employee::isActive)
                .count();
    }

    @Transactional
    public Map<String, Long> employeesPerContractType(Long id) {
        checkDepartmentAccess(id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + id));

        return department.getEmployees().stream()
                .filter(e -> e.isActive() && e.getContract() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getContract().getContractType().name(),
                        Collectors.counting()
                ));
    }

    @Transactional
    public List<ExpiringContractDTO> expiringContracts(Long id, int days) {
        checkDepartmentAccess(id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + id));

        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(days);

        return department.getEmployees().stream()
                .filter(e -> e.isActive() && e.getContract() != null)
                .filter(e -> {
                    Contract c = e.getContract();
                    return (c.getContractType() == ContractType.TEMPORARY
                            || c.getContractType() == ContractType.INTERNSHIP)
                            && c.getExpiryDate() != null
                            && c.getExpiryDate().isAfter(today)
                            && c.getExpiryDate().isBefore(limit);
                })
                .sorted(Comparator.comparing(e -> e.getContract().getExpiryDate()))
                .map(ExpiringContractDTO::fromEmployee)
                .toList();
    }

    @Transactional
    public Map<String, Long> activeEmployees(Long id) {
        checkDepartmentAccess(id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + id));

        return department.getEmployees().stream()
                .collect(Collectors.groupingBy(
                        e -> e.isActive() ? "Activos" : "Inactivos",
                        Collectors.counting()
                ));
    }

    @Transactional
    public Double averageTenure(Long id){
        checkDepartmentAccess(id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento con id: "+id+" no encontrado"));
        return department.getEmployees().stream()
                .filter(e-> e.getContract()!=null && e.isActive())
                .mapToLong(e-> ChronoUnit.MONTHS.between(
                        e.getContract().getCommencementDate(),LocalDate.now()
                ))
                .average()
                .orElse(0.0);
    }


    @Transactional
    public Double averageAge(Long id){
        checkDepartmentAccess(id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(()->new  ResourceNotFoundException("Departamento con id: "+id+" no encontrado"));
        return department.getEmployees().stream()
                .filter(Employee::isActive)
                .mapToDouble(e -> ChronoUnit.YEARS.between(e.getBirthDate(), LocalDate.now()))
                .average()
                .orElse(0.0);
    }

    @Transactional
    public Map<String, Long> employeesByPosition(Long departmentId) {
        checkDepartmentAccess(departmentId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + departmentId));

        return department.getEmployees().stream()
                .filter(e -> e.isActive() && !e.getPositions().isEmpty())
                .flatMap(e -> e.getPositions().stream())
                .collect(Collectors.groupingBy(
                        Position::getName,
                        Collectors.counting()
                ));
    }

    @Transactional
    public Map<String, Long> employeesByWorkplace(Long id) {
        checkDepartmentAccess(id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + id));

        return department.getEmployees().stream()
                .filter(e-> e.isActive() && e.getContract()!=null)
                .map(Employee::getContract)
                .collect(Collectors.groupingBy(
                        c->c.getWorkPlace().name(),
                        Collectors.counting()
                ));
    }

    @Transactional
    public SalaryRangeDTO salaryRange(Long id) {
        checkDepartmentAccess(id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + id));

        DoubleSummaryStatistics stats = department.getEmployees().stream()
                .filter(e -> e.isActive() && e.getContract() != null)
                .mapToDouble(e -> e.getContract().getSalary().doubleValue())
                .summaryStatistics();

        if(stats.getCount()==0){
            return new SalaryRangeDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        return new SalaryRangeDTO(
                BigDecimal.valueOf(stats.getMax()),
                BigDecimal.valueOf(stats.getMin()),
                BigDecimal.valueOf(stats.getAverage())
        );
    }

    @Transactional
    public Long newHires(Long id, int year){
        checkDepartmentAccess(id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + id));
        return department.getEmployees().stream()
                .filter(e->e.getContract()!=null && e.getContract().getCommencementDate().getYear() == year)
                .count();
        }

    @Transactional
    public Map<String,BigDecimal> budgetSituation(Long id){
        checkDepartmentAccess(id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + id));
        BigDecimal totalSalary = department.getEmployees().stream()
                .filter(e->e.isActive() && e.getContract() != null)
                .map(e->e.getContract().getSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String,BigDecimal> budgetSituation = new LinkedHashMap<>(); // Linked en lugar de hashmap para que se muestre en este orden.
        budgetSituation.put("Presupuesto anual del departamento",department.getAnnualBudget());
        budgetSituation.put("Masa salarial",totalSalary);
        budgetSituation.put("Diferencia",department.getAnnualBudget().subtract(totalSalary));
        return budgetSituation;
    }
}




