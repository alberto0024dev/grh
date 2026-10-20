package com.grh.grh.contract;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractResponseDTO {

    private Long id;
    private ContractType contractType;
    private LocalDate commencementDate;
    private LocalDate expiryDate;
    private int vacationDays;
    private BigDecimal salary; // necesario, pero ojo — en una app real el salario
    // es dato sensible. Un ROLE_EMPLEADO solo debería ver su propio salario, nunca el de otros. Esto lo controlas en el Service, no en el DTO.
    private WorkPlace workPlace;
    private WorkSchedule workSchedule;



    public static ContractResponseDTO fromEntity(Contract contract) {
        ContractResponseDTO contractResponseDTO = new ContractResponseDTO();
        contractResponseDTO.setId(contract.getId());
        contractResponseDTO.setContractType(contract.getContractType());
        contractResponseDTO.setCommencementDate(contract.getCommencementDate());
        contractResponseDTO.setExpiryDate(contract.getExpiryDate());
        contractResponseDTO.setVacationDays(contract.getVacationDays());
        contractResponseDTO.setSalary(contract.getSalary());
        contractResponseDTO.setWorkPlace(contract.getWorkPlace());
        contractResponseDTO.setWorkSchedule(contract.getWorkSchedule());
        return contractResponseDTO;
    }


}
