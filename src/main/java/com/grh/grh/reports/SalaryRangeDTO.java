package com.grh.grh.reports;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryRangeDTO {


    private BigDecimal max;
    private BigDecimal min;
    private BigDecimal average;


}
