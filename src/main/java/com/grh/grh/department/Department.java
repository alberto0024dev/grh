package com.grh.grh.department;


import com.grh.grh.employee.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name="departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"employees"})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String building;

    @Column(nullable = false)
    private int floor;

    @Column(nullable = false, length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(length = 16)
    private String contactPhone;

    @Column(nullable = false)
    private BigDecimal annualBudget;

    @Column
    private Long managerId;

    @OneToMany(mappedBy = "department")
    private List<Employee> employees;



    public static Department fromRequest(DepartmentRequestDTO departmentRequestDTO) {
        Department department = new Department();
        department.setName(departmentRequestDTO.getName().trim());
        department.setBuilding(departmentRequestDTO.getBuilding().trim());
        department.setFloor(departmentRequestDTO.getFloor());
        department.setAddress(departmentRequestDTO.getAddress().trim());
        department.setStatus(departmentRequestDTO.getStatus());
        department.setContactPhone(departmentRequestDTO.getContactPhone());
        department.setAnnualBudget(departmentRequestDTO.getAnnualBudget());
        department.setManagerId(departmentRequestDTO.getManagerId());
        return department;
    }

}
