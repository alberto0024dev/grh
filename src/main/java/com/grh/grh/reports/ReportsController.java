package com.grh.grh.reports;


import com.grh.grh.contract.ContractResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/average-salary/{id}")
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<Double> averageSalaryPerDepartment(@PathVariable Long id){
        Double averageSalary = reportsService.averageSalaryPerDepartment(id);
        return ResponseEntity.ok(averageSalary); // 200 operacion correcta devuelve datos.
    }

    @GetMapping("/employees-per-department/{id}")
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<Long> employeesPerDepartment(@PathVariable Long id){
        Long employeesPerDepartment = reportsService.employeesPerDepartment(id);
        return ResponseEntity.ok(employeesPerDepartment); //200 operaicon correcta devuelve datos.
    }

    @GetMapping("/employees-by-contract-type/{id}")
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<Map<String, Long>> employeesPerContractType(@PathVariable Long id){
        Map<String,Long> employeesByContractType=reportsService.employeesPerContractType(id);
        return ResponseEntity.ok(employeesByContractType); // 200 operacion correcta devuelve datos
    }

    @GetMapping("/expiring-contracts/{id}") // quedaria asi para pedir /api/reports/expiring-contracts/3?days=30
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<List<ExpiringContractDTO>> expiringContracts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(reportsService.expiringContracts(id,days));
    }

    @GetMapping("/active-employees/{id}")
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<Map<String, Long>> activeEmployees(@PathVariable Long id){
        return ResponseEntity.ok(reportsService.activeEmployees(id));
    }

    @GetMapping("/average-tenue/{id}")
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<Double> averageTenure(@PathVariable Long id){
        Double averageTenure = reportsService.averageTenure(id);
        return ResponseEntity.ok(averageTenure);
    }

    @GetMapping("/average-age/{id}")
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<Double> averageAge(@PathVariable Long id){
        Double averageAge = reportsService.averageAge(id);
        return ResponseEntity.ok(averageAge);
    }

    @GetMapping("/employees-by-position/{id}")
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<Map<String, Long>> employeesByPosition(@PathVariable Long id){
        Map<String,Long> employeesByPosition=reportsService.employeesByPosition(id);
        return ResponseEntity.ok(employeesByPosition);
    }

    @GetMapping("/salary-range/{id}")
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<SalaryRangeDTO> salaryRange(@PathVariable Long id){
        SalaryRangeDTO salaryRangeDTO=reportsService.salaryRange(id);
        return ResponseEntity.ok(salaryRangeDTO);
    }

    @GetMapping("/employees-by-workplace/{id}")
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<Map<String, Long>> employeesByWorkplace(@PathVariable Long id){
        Map<String,Long> employeesByWorkplace=reportsService.employeesByWorkplace(id);
        return ResponseEntity.ok(employeesByWorkplace);
    }

    @GetMapping("/new-hires/{id}/{year}")
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<Long> newHires(@PathVariable Long id, @PathVariable int year){
        Long newHires=reportsService.newHires(id, year);
        return  ResponseEntity.ok(newHires);
    }

    @GetMapping("/budget-situation/{id}")
    @PreAuthorize("hasAuthority('reports:view')")
    public ResponseEntity<Map<String, BigDecimal>> budgetSituation(@PathVariable Long id){
        Map<String, BigDecimal> budgetSituation=reportsService.budgetSituation(id);
        return ResponseEntity.ok(budgetSituation);
    }
}
