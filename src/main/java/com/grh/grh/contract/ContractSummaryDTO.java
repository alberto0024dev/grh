package com.grh.grh.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractSummaryDTO {
    private Long id;
    private ContractType contractType;
    private LocalDate commencementDate;
    private LocalDate expiryDate;


    public static ContractSummaryDTO fromEntity(Contract contract) {
        ContractSummaryDTO contractSummaryDTO = new ContractSummaryDTO();
        contractSummaryDTO.setId(contract.getId());
        contractSummaryDTO.setContractType(contract.getContractType());
        contractSummaryDTO.setCommencementDate(contract.getCommencementDate());
        contractSummaryDTO.setExpiryDate(contract.getExpiryDate());
        return contractSummaryDTO;
    }
}
