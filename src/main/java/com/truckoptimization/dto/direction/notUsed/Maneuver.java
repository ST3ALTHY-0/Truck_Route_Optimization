package com.truckoptimization.dto.direction.notUsed;

import java.util.List;
import lombok.Data;

@Data
public class Maneuver {
    private List<Double> location;
    private Integer bearingBefore;
    private Integer bearingAfter;
}