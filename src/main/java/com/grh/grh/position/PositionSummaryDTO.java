package com.grh.grh.position;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionSummaryDTO {
    private Long id;
    private String name;
    private JobCategory jobCategory;



    public static PositionSummaryDTO fromEntity(Position entity) {
        PositionSummaryDTO dto = new PositionSummaryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setJobCategory(entity.getJobCategory());
        return dto;
    }
}
