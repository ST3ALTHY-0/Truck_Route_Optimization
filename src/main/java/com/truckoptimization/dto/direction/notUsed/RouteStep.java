package com.truckoptimization.dto.direction.notUsed;

import java.util.List;
import lombok.Data;

@Data
public class RouteStep {
    private double distance;
    private double duration;
    private int type;
    private String instruction;
    private String name;
    private Integer exitNumber;
    private List<Integer> exitBearings;
    private List<Integer> wayPoints;
    private Maneuver maneuver;
}