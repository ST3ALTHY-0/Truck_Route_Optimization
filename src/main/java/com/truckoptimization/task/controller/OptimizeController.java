package com.truckoptimization.task.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.truckoptimization.common.config.RoutingConfig;
import com.truckoptimization.common.config.RoutingConfigNew;
import com.truckoptimization.dto.database.nosql.feature.compiledRouteResult.CompiledRouteResultService;
import com.truckoptimization.dto.results.CompiledResults;
import com.truckoptimization.dto.waypoint.Waypoint;
import com.truckoptimization.task.controller.support.OptimizationFormSupportService;
import com.truckoptimization.task.controller.support.RouteGeometryService;
import com.truckoptimization.task.optimizeRoute.RouteOptimizationService;

@Controller
@RequestMapping("/optimize")
public class OptimizeController {
    // This controller will handle optimization requests and related endpoints

    private final CompiledRouteResultService compiledRouteResultService;
    private final RouteOptimizationService routeOptimizationService;
    private final OptimizationFormSupportService optimizationFormSupportService;
    private final RouteGeometryService routeGeometryService;

    OptimizeController(RouteOptimizationService routeOptimizationService,
            OptimizationFormSupportService optimizationFormSupportService,
            RouteGeometryService routeGeometryService, 
            CompiledRouteResultService compiledRouteResultService) {
        this.routeOptimizationService = routeOptimizationService;
        this.optimizationFormSupportService = optimizationFormSupportService;
        this.routeGeometryService = routeGeometryService;
        this.compiledRouteResultService = compiledRouteResultService;
    }
    @GetMapping({"", "/"})
    public String optimizeGetFallback() {
        return "redirect:/";
    }

    @PostMapping(value = {"", "/"}, params = "csvFile")
    public String optimizeWithCsv(@RequestParam("csvFile") MultipartFile file, RoutingConfig routingConfig,
            Model model) {
        try {
            // List<CompiledResults> results = routeOptimizationService.optimizeWithCsv(file, routingConfig);
        } catch (Exception e) {
            String message = "An error occurred while processing the CSV file: " + e.getMessage();
            model.addAttribute("error", message);
            model.addAttribute("errorMessage", message);
            return "home";
        }
        return "home";
    }

    // TODO: with how we have changed manual input, how we handle address input can
    // likely be handled more gracefully

    //TODO: use an object to capture all the form inputs instead of 20+ individual parameters, but I think i ran into a problem with it
    //TODO: Do what we do with globalMaxRouteMiles and set default values in the form, and then just use those default values
    // additionally how we handle Routing config might need edited, with more
    // complex inputs, business logic and error checking need to occur
    @PostMapping(value = {"", "/"}, params = "!csvFile")
    public String optimizeWithForm(
            @RequestParam(value = "manualInput", required = false) String manualInput,
            @RequestParam(value = "waypointsJson", required = false) String waypointsJson,
            @RequestParam(value = "truckParametersJson", required = false) String truckParametersJson,
            // Tier 1: Vehicle Defaults
            @RequestParam(value = "defaultTruckCapacity", defaultValue = "10") int defaultTruckCapacity,
            @RequestParam(value = "defaultMaxRouteMiles", defaultValue = "1000") double defaultMaxRouteMiles,
            @RequestParam(value = "defaultMaxRouteDurationSeconds", defaultValue = "43200") int defaultMaxRouteDurationSeconds,
            @RequestParam(value = "defaultCostPerMile", defaultValue = "1.75") double defaultCostPerMile,
            @RequestParam(value = "defaultCostPerHour", defaultValue = "65") double defaultCostPerHour,
            @RequestParam(value = "defaultFixedCostPerTruck", defaultValue = "100000") double defaultFixedCostPerTruck,
            @RequestParam(value = "defaultServiceTimeMultiplier", defaultValue = "1.0") double defaultServiceTimeMultiplier,
            // Tier 2: Problem Settings
            @RequestParam(value = "numberOfTrucks", defaultValue = "10") int numberOfTrucks,
            @RequestParam(value = "depotStartTimeSeconds", defaultValue = "0") int depotStartTimeSeconds,
            @RequestParam(value = "depotEndTimeSeconds", defaultValue = "86400") int depotEndTimeSeconds,
            @RequestParam(value = "depotStartTime", required = false) String depotStartTime,
            @RequestParam(value = "depotEndTime", required = false) String depotEndTime,
            @RequestParam(value = "globalMaxRouteMiles", defaultValue = "1500") double globalMaxRouteMiles,
            @RequestParam(value = "globalMaxRouteDurationHours", required = false) String globalMaxRouteDurationHours,
            @RequestParam(value = "globalMaxRouteDurationSeconds", defaultValue = "43200") int globalMaxRouteDurationSeconds,
            @RequestParam(value = "timeWindowSlackMinutes", required = false) String timeWindowSlackMinutes,
            @RequestParam(value = "timeWindowSlackSeconds", defaultValue = "3600") int timeWindowSlackSeconds,
            @RequestParam(value = "useTimeConstraints", required = false) Boolean useTimeConstraints,
            // Tier 3: Optimizer Presets
            @RequestParam(value = "optimizationGoal", defaultValue = "BALANCED") String optimizationGoalStr,
            @RequestParam(value = "solveSpeed", defaultValue = "NORMAL") String solveSpeedStr,
            @RequestParam(value = "routeBalanceCoefficient", defaultValue = "100") int routeBalanceCoefficient,
            Model model) {

        model.addAttribute("formState", buildFormState(
                manualInput,
                waypointsJson,
                truckParametersJson,
                defaultTruckCapacity,
                defaultMaxRouteMiles,
                defaultMaxRouteDurationSeconds,
                defaultCostPerMile,
                defaultCostPerHour,
                defaultFixedCostPerTruck,
                defaultServiceTimeMultiplier,
                numberOfTrucks,
                depotStartTime,
                depotEndTime,
                depotStartTimeSeconds,
                depotEndTimeSeconds,
                globalMaxRouteMiles,
                globalMaxRouteDurationHours,
                globalMaxRouteDurationSeconds,
                timeWindowSlackMinutes,
                timeWindowSlackSeconds,
                useTimeConstraints,
                optimizationGoalStr,
                solveSpeedStr,
                routeBalanceCoefficient));

        model.addAttribute("globalMaxRouteMiles", globalMaxRouteMiles);

        RoutingConfigNew routingConfig = optimizationFormSupportService.buildRoutingConfig(
                defaultTruckCapacity,
                defaultMaxRouteMiles,
                defaultMaxRouteDurationSeconds,
                defaultCostPerMile,
                defaultCostPerHour,
                defaultFixedCostPerTruck,
                defaultServiceTimeMultiplier,
                depotStartTimeSeconds,
                depotEndTimeSeconds,
                globalMaxRouteMiles,
                globalMaxRouteDurationSeconds,
                timeWindowSlackSeconds,
                useTimeConstraints,
                optimizationGoalStr,
                solveSpeedStr,
                routeBalanceCoefficient);    
        List<Waypoint> waypoints = optimizationFormSupportService
                .parseAddressAndDemandInput(manualInput, waypointsJson);

        // Convert RoutingConfigNew to old RoutingConfig for backwards compatibility
        RoutingConfig oldConfig = routingConfig.toRoutingConfig(numberOfTrucks);

        CompiledResults result = routeOptimizationService.optimizeRoutesWithWaypoints(waypoints, oldConfig);

        if (result == null) {
            String message = "No valid routes found for the given addresses and demands.";
            model.addAttribute("error", message);
            model.addAttribute("errorMessage", message);
            return "home";
        }

        routeGeometryService.addGeometry(result);

        compiledRouteResultService.saveCompiledResults(result);


        try {
            String resultsJson = optimizationFormSupportService.toResultsJson(result);
            model.addAttribute("resultsJson", resultsJson);
            result.printRouteResults();
        } catch (JsonProcessingException e) {
            String message = "An error occurred while processing the results: " + e.getMessage();
            model.addAttribute("error", message);
            model.addAttribute("errorMessage", message);
        }

        return "home";
    }

    private Map<String, Object> buildFormState(
            String manualInput,
            String waypointsJson,
            String truckParametersJson,
            int defaultTruckCapacity,
            double defaultMaxRouteMiles,
            int defaultMaxRouteDurationSeconds,
            double defaultCostPerMile,
            double defaultCostPerHour,
            double defaultFixedCostPerTruck,
            double defaultServiceTimeMultiplier,
            int numberOfTrucks,
            String depotStartTime,
            String depotEndTime,
            int depotStartTimeSeconds,
            int depotEndTimeSeconds,
            double globalMaxRouteMiles,
            String globalMaxRouteDurationHours,
            int globalMaxRouteDurationSeconds,
            String timeWindowSlackMinutes,
            int timeWindowSlackSeconds,
            Boolean useTimeConstraints,
            String optimizationGoal,
            String solveSpeed,
            int routeBalanceCoefficient) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("manualInput", manualInput == null ? "" : manualInput);
        state.put("waypointsJson", waypointsJson == null ? "" : waypointsJson);
        state.put("truckParametersJson", truckParametersJson == null ? "" : truckParametersJson);
        state.put("defaultTruckCapacity", defaultTruckCapacity);
        state.put("defaultMaxRouteMiles", defaultMaxRouteMiles);
        state.put("defaultMaxRouteDurationSeconds", defaultMaxRouteDurationSeconds);
        state.put("defaultCostPerMile", defaultCostPerMile);
        state.put("defaultCostPerHour", defaultCostPerHour);
        state.put("defaultFixedCostPerTruck", defaultFixedCostPerTruck);
        state.put("defaultServiceTimeMultiplier", defaultServiceTimeMultiplier);
        state.put("numberOfTrucks", numberOfTrucks);
        state.put("depotStartTime", depotStartTime == null ? "" : depotStartTime);
        state.put("depotEndTime", depotEndTime == null ? "" : depotEndTime);
        state.put("depotStartTimeSeconds", depotStartTimeSeconds);
        state.put("depotEndTimeSeconds", depotEndTimeSeconds);
        state.put("globalMaxRouteMiles", globalMaxRouteMiles);
        state.put("globalMaxRouteDurationHours", globalMaxRouteDurationHours == null ? "" : globalMaxRouteDurationHours);
        state.put("globalMaxRouteDurationSeconds", globalMaxRouteDurationSeconds);
        state.put("timeWindowSlackMinutes", timeWindowSlackMinutes == null ? "" : timeWindowSlackMinutes);
        state.put("timeWindowSlackSeconds", timeWindowSlackSeconds);
        state.put("useTimeConstraints", useTimeConstraints != null && useTimeConstraints);
        state.put("optimizationGoal", optimizationGoal == null ? "BALANCED" : optimizationGoal);
        state.put("solveSpeed", solveSpeed == null ? "NORMAL" : solveSpeed);
        state.put("routeBalanceCoefficient", routeBalanceCoefficient);
        return state;
    }

}
