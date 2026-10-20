package com.grh.grh.department;

import com.grh.grh.employee.EmployeeSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentResponseDTO {

    private Long id;
    private String name;
    private String building;
    private int floor;
    private String address;
    private Status status;
    private String contactPhone;
    private BigDecimal annualBudget;
    private Long managerId;
    private List<EmployeeSummaryDTO> employees;


    public static DepartmentResponseDTO fromEntity(Department department) {
        DepartmentResponseDTO departmentResponseDTO = new DepartmentResponseDTO();
        departmentResponseDTO.setId(department.getId());
        departmentResponseDTO.setName(department.getName());
        departmentResponseDTO.setBuilding(department.getBuilding());
        departmentResponseDTO.setFloor(department.getFloor());
        departmentResponseDTO.setAddress(department.getAddress());
        departmentResponseDTO.setStatus(department.getStatus());
        departmentResponseDTO.setContactPhone(department.getContactPhone());
        departmentResponseDTO.setAnnualBudget(department.getAnnualBudget());
        departmentResponseDTO.setManagerId(department.getManagerId());
        return departmentResponseDTO;
    }

}
