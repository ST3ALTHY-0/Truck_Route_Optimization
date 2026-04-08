package com.truckoptimization.dto.direction;

import java.util.List;

import lombok.Data;

@Data
public class ExtraDetail {

    private List<SummaryEntry> summary;
    private List<List<Integer>> values;
    
}
