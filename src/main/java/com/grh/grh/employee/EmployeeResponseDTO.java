package com.grh.grh.employee;


import com.grh.grh.contract.ContractSummaryDTO;
import com.grh.grh.department.DepartmentSummaryDTO;
import com.grh.grh.position.PositionSummaryDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponseDTO {

    private Long id;
    private String name;
    private String firstSurname;
    private String secondSurname;
    private String address;
    private String emergencyContact;
    private String phoneNumber;
    private boolean active;
    private String email;
    private LocalDate birthDate;
    private String temporaryPassword;

    private ContractSummaryDTO contract;
    private List<PositionSummaryDTO> positions;
    private DepartmentSummaryDTO department;


    public static EmployeeResponseDTO fromEntity(Employee employee) {
        EmployeeResponseDTO employeeResponseDTO = new EmployeeResponseDTO();
        basicLoad(employee, employeeResponseDTO);
        if (employee.getContract() != null) {
            employeeResponseDTO.setContract(ContractSummaryDTO.fromEntity(employee.getContract()));
        }
        if (employee.getDepartment() != null) {
            employeeResponseDTO.setDepartment(DepartmentSummaryDTO.fromEntity(employee.getDepartment()));
        }
        employeeResponseDTO.setPositions(
                employee.getPositions() != null
                        ? employee.getPositions().stream().map(PositionSummaryDTO::fromEntity).toList()
                        : List.of()
        );
        return employeeResponseDTO;

    }

    private static void basicLoad(Employee employee, EmployeeResponseDTO employeeResponseDTO) {
        employeeResponseDTO.setId(employee.getId());
        employeeResponseDTO.setName(employee.getName());
        employeeResponseDTO.setFirstSurname(employee.getFirstSurname());
        employeeResponseDTO.setSecondSurname(employee.getSecondSurname());
        employeeResponseDTO.setAddress(employee.getAddress());
        employeeResponseDTO.setEmergencyContact(employee.getEmergencyContact());
        employeeResponseDTO.setPhoneNumber(employee.getPhoneNumber());
        employeeResponseDTO.setActive(employee.isActive());
        employeeResponseDTO.setEmail(employee.getEmail());
        employeeResponseDTO.setBirthDate(employee.getBirthDate());
    }
}
