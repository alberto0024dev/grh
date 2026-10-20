package com.grh.grh.contract;


import com.grh.grh.employee.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;

@Entity
@Table(name="contracts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractType contractType;

    @Column(nullable = false)
    private LocalDate commencementDate;

    private LocalDate expiryDate; // opcional (indefinido).

    @Column(nullable = false)
    private int vacationDays;

    @Column(nullable = false)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkPlace workPlace;

    @Embedded
    private WorkSchedule workSchedule;

    @OneToOne(mappedBy = "contract")
    private Employee employee;


    public static Contract toEntity(ContractRequestDTO contractRequestDTO, Employee employee) {
        Contract contract = new Contract();
        contract.setContractType(contractRequestDTO.getContractType());
        contract.setCommencementDate(contractRequestDTO.getCommencementDate());
        contract.setExpiryDate(contractRequestDTO.getExpiryDate());
        contract.setVacationDays(contractRequestDTO.getVacationDays());
        contract.setSalary(contractRequestDTO.getSalary());
        contract.setWorkPlace(contractRequestDTO.getWorkPlace());
        contract.setWorkSchedule(contractRequestDTO.getWorkSchedule());
        contract.setEmployee(employee);
        return contract;

    }



}
