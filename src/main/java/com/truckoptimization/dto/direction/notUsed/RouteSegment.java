package com.truckoptimization.dto.direction.notUsed;

import java.util.List;
import lombok.Data;

@Data
public class RouteSegment {
    private double distance; // Distance of the segment in meters
    private double duration; // Duration of the segment in seconds
    private List<RouteStep> steps; // List of steps in this segment
    private double detourfactor;
    private double percentage;
    private double avgspeed;
    private double ascent;
    private double descent;
}