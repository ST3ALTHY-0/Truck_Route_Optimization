package com.truckoptimization.task.controller.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.truckoptimization.common.config.RoutingConfigNew;
import com.truckoptimization.dto.OptimizerGoal;
import com.truckoptimization.dto.results.CompiledResults;
import com.truckoptimization.dto.waypoint.Waypoint;
import com.truckoptimization.exception.InvalidInputException;

@Service
public class OptimizationFormSupportService {

    public RoutingConfigNew buildRoutingConfig(
            int defaultTruckCapacity,
            double defaultMaxRouteMiles,
            int defaultMaxRouteDurationSeconds,
            double defaultCostPerMile,
            double defaultCostPerHour,
            double defaultFixedCostPerTruck,
            double defaultServiceTimeMultiplier,
            int depotStartTimeSeconds,
            int depotEndTimeSeconds,
            double globalMaxRouteMiles,
            int globalMaxRouteDurationSeconds,
            int timeWindowSlackSeconds,
            Boolean useTimeConstraints,
            String optimizationGoalStr,
            String solveSpeedStr,
            int routeBalanceCoefficient) {

        RoutingConfigNew routingConfig = new RoutingConfigNew();

        routingConfig.setDefaultTruckCapacity(defaultTruckCapacity);
        routingConfig.setDefaultMaxRouteMiles(defaultMaxRouteMiles);
        routingConfig.setDefaultMaxRouteDurationSeconds(defaultMaxRouteDurationSeconds);
        routingConfig.setDefaultCostPerMile(defaultCostPerMile);
        routingConfig.setDefaultCostPerHour(defaultCostPerHour);
        routingConfig.setDefaultFixedCostPerTruck(defaultFixedCostPerTruck);
        routingConfig.setDefaultServiceTimeMultiplier(defaultServiceTimeMultiplier);

        routingConfig.setDepotStartTimeSeconds(depotStartTimeSeconds);
        routingConfig.setDepotEndTimeSeconds(depotEndTimeSeconds);
        routingConfig.setGlobalMaxRouteMiles(globalMaxRouteMiles);
        routingConfig.setGlobalMaxRouteDurationSeconds(globalMaxRouteDurationSeconds);
        routingConfig.setTimeWindowSlackSeconds(timeWindowSlackSeconds);
        if (useTimeConstraints != null) {
            routingConfig.setUseTimeConstraints(useTimeConstraints);
        }

        try {
            OptimizerGoal goal = OptimizerGoal.valueOf(optimizationGoalStr);
            routingConfig.setOptimizationGoal(goal);
        } catch (IllegalArgumentException e) {
            routingConfig.setOptimizationGoal(OptimizerGoal.BALANCED);
        }

        try {
            RoutingConfigNew.SolveSpeed speed = RoutingConfigNew.SolveSpeed.valueOf(solveSpeedStr);
            routingConfig.setSolveSpeed(speed);
        } catch (IllegalArgumentException e) {
            routingConfig.setSolveSpeed(RoutingConfigNew.SolveSpeed.NORMAL);
        }

        routingConfig.setRouteBalanceCoefficient(routeBalanceCoefficient);
        routingConfig.applyOptimizationGoal();
        routingConfig.applySolveSpeed();

        return routingConfig;
    }

    public List<Waypoint> parseAddressAndDemandInput(String manualInput, String waypointsJson) {
        List<String> addresses = new ArrayList<>();
        List<Integer> demands = new ArrayList<>();

        boolean useWaypoints = waypointsJson != null && !waypointsJson.isEmpty();

        if (useWaypoints) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> waypointsData;
            try {
                waypointsData = mapper.readValue(waypointsJson, Map.class);
            } catch (JsonProcessingException e) {
                throw new InvalidInputException("Invalid waypoint payload format.");
            }

            Map<String, Object> startWaypoint = (Map<String, Object>) waypointsData.get("start");
            if (startWaypoint != null && startWaypoint.get("address") != null) {
                String address = (String) startWaypoint.get("address");
                if (!address.trim().isEmpty()) {
                    addresses.add(address);
                    demands.add(0);
                }
            }

            List<Map<String, Object>> stops = (List<Map<String, Object>>) waypointsData.get("stops");
            if (stops != null) {
                for (Map<String, Object> stop : stops) {
                    String address = (String) stop.get("address");
                    Integer demand = ((Number) stop.get("demand")).intValue();
                    if (!address.trim().isEmpty()) {
                        addresses.add(address);
                        demands.add(demand);
                    }
                }
            }

            Map<String, Object> endWaypoint = (Map<String, Object>) waypointsData.get("end");
            if (endWaypoint != null && endWaypoint.get("address") != null) {
                String address = (String) endWaypoint.get("address");
                Object startAddress = startWaypoint == null ? null : startWaypoint.get("address");
                if (!address.trim().isEmpty() && !address.equals(startAddress)) {
                    addresses.add(address);
                    demands.add(0);
                }
            }
        }
        if (addresses.isEmpty()) {
            throw new InvalidInputException("No valid delivery locations found. Please check your input.");
        }

        List<Waypoint> waypoints = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i++) {
            waypoints.add(new Waypoint(addresses.get(i), demands.get(i)));
        }
        return waypoints;
    }

    public String toResultsJson(CompiledResults result) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.writeValueAsString(result);
    }



}
