package com.truckoptimization.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "routing")
@Data
public class RoutingConfig {
    private int numberOfTrucks;
    private int truckCapacity;
    private double maxDistanceMiles;
    //private double optimalDistanceMiles;
    private int calculationTime;
    // private double penaltyPerMileOver;
    // private double penaltyPerMileUnder;
    private int costOfAddingTruck;

    /**
     * Weight to convert time (seconds) into an equivalent distance cost (meters).
     * Example: a value of 1 means 1 second of time counts as 1 meter of distance.
     * 1 second ≈ 24.6 meters of driving at 55mph
     */
    private double timeWeight = 20.0;

    // Time-based routing fields
    private long[][] travelTimeMatrixSeconds;
    private long[][] distanceMatrix;
    private long[][] timeWindowsSeconds;
    private long[] serviceTimesSeconds;

    //TODO: neccissary defaults for now, move to GUI later
    private boolean useTimeConstraints = false;
    private long depotStartTimeSeconds = 0;
    private long depotEndTimeSeconds = 86400;
    private long maxRouteDurationSeconds = 43200;
    // Maximum allowed waiting slack (seconds) at stops when time windows are enabled
    private long timeWindowSlackSeconds = 600; // default 10 minutes

    public long convertMilesToMeters(double Miles) {
        return (long) (Miles * 1609.344);
    }

    
}
