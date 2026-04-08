package com.truckoptimization.task.optimizeRoute;

import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.truckoptimization.common.config.RoutingConfig;

public class RoutingContext {

    private final int numVehicles;
    private final long[] vehicleCapacities;
    private final int demandCallbackIndex;
    private final RoutingModel routing;
    private final int distanceCallbackIndex;
    private final Integer timeCallbackIndex;
    private final RoutingIndexManager manager;
    private final RoutingConfig routingConfig;
    private final boolean useTimes;
    private final long[][] timeWindows;
    private final long depotStart;
    private final long depotEnd;
    private final long maxRouteDuration;

    public RoutingContext(
            int numVehicles,
            long[] vehicleCapacities,
            int demandCallbackIndex,
            RoutingModel routing,
            int distanceCallbackIndex,
            Integer timeCallbackIndex,
            RoutingIndexManager manager,
            RoutingConfig routingConfig,
            boolean useTimes,
            long[][] timeWindows,
            long depotStart,
            long depotEnd,
            long maxRouteDuration) {
        this.numVehicles = numVehicles;
        this.vehicleCapacities = vehicleCapacities;
        this.demandCallbackIndex = demandCallbackIndex;
        this.routing = routing;
        this.distanceCallbackIndex = distanceCallbackIndex;
        this.timeCallbackIndex = timeCallbackIndex;
        this.manager = manager;
        this.routingConfig = routingConfig;
        this.useTimes = useTimes;
        this.timeWindows = timeWindows;
        this.depotStart = depotStart;
        this.depotEnd = depotEnd;
        this.maxRouteDuration = maxRouteDuration;
    }

    public int getNumVehicles() {
        return numVehicles;
    }

    public long[] getVehicleCapacities() {
        return vehicleCapacities;
    }

    public int getDemandCallbackIndex() {
        return demandCallbackIndex;
    }

    public RoutingModel getRouting() {
        return routing;
    }

    public int getDistanceCallbackIndex() {
        return distanceCallbackIndex;
    }

    public Integer getTimeCallbackIndex() {
        return timeCallbackIndex;
    }

    public RoutingIndexManager getManager() {
        return manager;
    }

    public RoutingConfig getRoutingConfig() {
        return routingConfig;
    }

    public boolean isUseTimes() {
        return useTimes;
    }

    public long[][] getTimeWindows() {
        return timeWindows;
    }

    public long getDepotStart() {
        return depotStart;
    }

    public long getDepotEnd() {
        return depotEnd;
    }

    public long getMaxRouteDuration() {
        return maxRouteDuration;
    }
}
