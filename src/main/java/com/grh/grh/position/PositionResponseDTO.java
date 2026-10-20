package com.grh.grh.position;

import com.grh.grh.employee.EmployeeSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionResponseDTO {

    private Long id;

    private String name;

    private JobCategory jobCategory;

    private List<EmployeeSummaryDTO> employees;


    public static PositionResponseDTO fromEntity(Position position){
        PositionResponseDTO positionResponseDTO = new PositionResponseDTO();
        positionResponseDTO.setId(position.getId());
        positionResponseDTO.setName(position.getName());
        positionResponseDTO.setJobCategory(position.getJobCategory());
        return positionResponseDTO;
    }
}
