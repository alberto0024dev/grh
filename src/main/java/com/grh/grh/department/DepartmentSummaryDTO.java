package com.grh.grh.department;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentSummaryDTO {

    private Long id;
    private String name;
    private String building;
    private String address;
    private String contactPhone;


    public static DepartmentSummaryDTO fromEntity(Department department) {
        DepartmentSummaryDTO dto = new DepartmentSummaryDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setBuilding(department.getBuilding());
        dto.setAddress(department.getAddress());
        dto.setContactPhone(department.getContactPhone());
        return dto;
    }
}
