package com.grh.grh.reports;

import com.grh.grh.contract.Contract;
import com.grh.grh.contract.ContractType;
import com.grh.grh.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpiringContractDTO {
    private Long contractId;
    private String employeeName;
    private String employeeFirstSurname;
    private ContractType contractType;
    private LocalDate expiryDate;
    private long daysLeft;

    public static ExpiringContractDTO fromEmployee(Employee employee) {
        Contract c = employee.getContract();
        return new ExpiringContractDTO(
                c.getId(),
                employee.getName(),
                employee.getFirstSurname(),
                c.getContractType(),
                c.getExpiryDate(),
                ChronoUnit.DAYS.between(LocalDate.now(), c.getExpiryDate())
        );
    }
}
