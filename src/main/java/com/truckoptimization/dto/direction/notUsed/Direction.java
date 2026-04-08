package com.truckoptimization.dto.direction.notUsed;

import java.util.List;

import lombok.Data;

@Data
public class Direction {

    private DirectionSummary directionSummary;
    private double ascent; // Total ascent in meters
    private double descent; // Total descent in meters
    private List<RouteSegment> segments; // If you want segment/step info
    private List<String> warnings; // If you want to show warnings

}
