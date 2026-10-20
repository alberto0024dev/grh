package com.grh.grh.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeSummaryDTO {
    private Long id;
    private String name;
    private String firstSurname;

    public static EmployeeSummaryDTO fromEntity(Employee employee) {
        EmployeeSummaryDTO employeeSummaryDTO = new EmployeeSummaryDTO();
        employeeSummaryDTO.setId(employee.getId());
        employeeSummaryDTO.setName(employee.getName());
        employeeSummaryDTO.setFirstSurname(employee.getFirstSurname());
        return employeeSummaryDTO;
    }
}