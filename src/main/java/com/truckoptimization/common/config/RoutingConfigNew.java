package com.truckoptimization.common.config;

import com.truckoptimization.dto.OptimizerGoal;

import lombok.Data;

@Data
public class RoutingConfigNew {

    private int defaultTruckCapacity = 10;
    private double defaultFixedCostPerTruck = 100000;
    private double defaultMaxRouteMiles = 1000;
    private long defaultMaxRouteDurationSeconds = 43200;
    private double defaultCostPerMile = 1.75;
    private double defaultCostPerHour = 65;
    private double defaultServiceTimeMultiplier = 1.0;

    private double globalMaxRouteMiles = 1500;
    private long globalMaxRouteDurationSeconds = 43200;
    private long depotStartTimeSeconds = 0;
    private long depotEndTimeSeconds = 86400;
    private long timeWindowSlackSeconds = 3600;
    private double penaltyForSkippedStop = Double.POSITIVE_INFINITY;

    private OptimizerGoal optimizationGoal = OptimizerGoal.BALANCED;
    private SolveSpeed solveSpeed = SolveSpeed.NORMAL;
    private double routeBalanceCoefficient = 100;
    private double timeVsDistanceWeight = 10;

    private boolean useTimeConstraints = false;
    private int calculationTimeSeconds = 30;

    public long convertMilesToMeters(double miles) {
        return (long) (miles * 1609.344);
    }

    public enum SolveSpeed {
        FAST(10),
        NORMAL(30),
        BEST(120);

        public final int seconds;

        SolveSpeed(int seconds) {
            this.seconds = seconds;
        }
    }

    public void applyOptimizationGoal() {
        switch (optimizationGoal) {
            case CHEAPEST:
                timeVsDistanceWeight = 2;
                routeBalanceCoefficient = 50;
                break;
            case FASTEST:
                timeVsDistanceWeight = 30;
                routeBalanceCoefficient = 80;
                break;
            case BALANCED:
                timeVsDistanceWeight = 15;
                routeBalanceCoefficient = 100;
                break;
            case FEWEST_TRUCKS:
                timeVsDistanceWeight = 8;
                routeBalanceCoefficient = 150;
                break;
        }
    }

    public void applySolveSpeed() {
        this.calculationTimeSeconds = solveSpeed.seconds;
    }

    public RoutingConfig toRoutingConfig(int numberOfTrucks) {
        RoutingConfig config = new RoutingConfig();
        config.setNumberOfTrucks(numberOfTrucks);
        config.setTruckCapacity(defaultTruckCapacity);
        config.setMaxDistanceMiles(globalMaxRouteMiles);
        config.setCalculationTime(calculationTimeSeconds);
        config.setCostOfAddingTruck((int) defaultFixedCostPerTruck);
        config.setTimeWeight(timeVsDistanceWeight);
        config.setUseTimeConstraints(useTimeConstraints);
        config.setDepotStartTimeSeconds(depotStartTimeSeconds);
        config.setDepotEndTimeSeconds(depotEndTimeSeconds);
        config.setMaxRouteDurationSeconds(globalMaxRouteDurationSeconds);
        config.setTimeWindowSlackSeconds(timeWindowSlackSeconds);
        return config;
    }
}
