package com.truckoptimization.task.optimizeRoute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.LocalSearchMetaheuristic;
import com.google.ortools.constraintsolver.RoutingDimension;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.RoutingSearchStatus.Value;
import com.google.protobuf.Duration;
import com.truckoptimization.common.config.RoutingConfig;
import com.truckoptimization.dto.results.RouteResult;
import com.truckoptimization.exception.NoSolutionFoundException;
import com.truckoptimization.task.api.orsApi.DistanceMatrixCalculation;

import com.google.ortools.constraintsolver.main;

//TODO check for feasibility before trying to solve

@Service
public class OptimizeRoutesWithORTools {

    public List<RouteResult> optimizeRoutes(
            long[][] distanceMatrixCords,
            int[] demands,
            RoutingConfig routingConfig
    ) {

        System.out.println("Running Optimization Calculations...");
        System.out.println("Inputs Summary:");
        System.out.println("  numLocations = " + (distanceMatrixCords == null ? 0 : distanceMatrixCords.length));
        System.out.println("  demandsLen   = " + (demands == null ? 0 : demands.length));
        System.out.println("  vehicles     = " + routingConfig.getNumberOfTrucks());
        System.out.println("  truckCapacity= " + routingConfig.getTruckCapacity());

        int depot = 0;
        int numVehicles = routingConfig.getNumberOfTrucks();
        int numLocations = distanceMatrixCords.length;

        boolean useTimes = routingConfig.isUseTimeConstraints();

         
        // TIME DATA 
         
                long[][] travelTimeMatrix = null;
                long[][] timeWindows = null;
                long[] serviceTimes = null;
                long depotStart = 0;
                long depotEnd = 0;
                long maxRouteDuration = 0;

                if (useTimes) {
                        travelTimeMatrix = routingConfig.getTravelTimeMatrixSeconds();
                        timeWindows = routingConfig.getTimeWindowsSeconds();
                        long[] serviceTimesTemp = routingConfig.getServiceTimesSeconds();
                        if (serviceTimesTemp == null) {
                                System.out.println("[DEBUG] serviceTimes is null, using zeros");
                                serviceTimes = new long[numLocations];
                        } else {
                                serviceTimes = serviceTimesTemp;
                        }

                        depotStart = routingConfig.getDepotStartTimeSeconds();
                        depotEnd = routingConfig.getDepotEndTimeSeconds();
                        maxRouteDuration = routingConfig.getMaxRouteDurationSeconds();

                        if (travelTimeMatrix != null) {
                                DistanceMatrixCalculation.printDistanceMatrix(travelTimeMatrix);
                        }


                        if (depotEnd <= depotStart) {
                                System.out.println("[WARN] depotEndTimeSeconds (" + depotEnd + ") <= depotStartTimeSeconds (" + depotStart + ") - vehicles will have no valid time window!");
                        }
                        if (maxRouteDuration <= 0) {
                                System.out.println("[WARN] maxRouteDurationSeconds is " + maxRouteDuration + " - routes cannot take any time, solver will likely fail!");
                        }
                } else {
                        System.out.println("[INFO] Time constraints disabled (useTimeConstraints=false). Skipping time matrices and windows.");
                }

                if (distanceMatrixCords != null) {
                        DistanceMatrixCalculation.printDistanceMatrix(distanceMatrixCords);
                }

         
        // VEHICLE CAPACITY
         
        long[] vehicleCapacities = new long[numVehicles];
        Arrays.fill(vehicleCapacities, routingConfig.getTruckCapacity());

        // Validate all inputs before creating callbacks to avoid NPEs
        validateInputs(distanceMatrixCords, demands, useTimes, travelTimeMatrix, serviceTimes, timeWindows, numLocations);

        RoutingIndexManager manager =
                new RoutingIndexManager(numLocations, numVehicles, depot);
        RoutingModel routing = new RoutingModel(manager);

         
        // DISTANCE CALLBACK
         
        final int distanceCallbackIndex =
                routing.registerTransitCallback((long fromIndex, long toIndex) -> {
                    int fromNode = manager.indexToNode(fromIndex);
                    int toNode = manager.indexToNode(toIndex);
                    return distanceMatrixCords[fromNode][toNode];
                });

        routing.setArcCostEvaluatorOfAllVehicles(distanceCallbackIndex);

         
        // DEMAND CALLBACK
         
        final int demandCallbackIndex =
                routing.registerUnaryTransitCallback((long fromIndex) -> {
                    int fromNode = manager.indexToNode(fromIndex);
                    return demands[fromNode];
                });

         
        // TIME CALLBACK (NEW) - only if enabled
         
        Integer timeCallbackIndex = null;
        if (useTimes) {
            final long[][] travelTimeMatrixFinal = travelTimeMatrix;
            final long[] serviceTimesFinal = serviceTimes;
                        // Time-only callback for the Time dimension
                        timeCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
                                int from = manager.indexToNode(fromIndex);
                                int to = manager.indexToNode(toIndex);
                                return travelTimeMatrixFinal[from][to] + serviceTimesFinal[from];
                        });

                        // Composite cost: distance + (timeWeight * time)
                        final int costCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
                                int from = manager.indexToNode(fromIndex);
                                int to = manager.indexToNode(toIndex);

                                long distanceCost = distanceMatrixCords[from][to];
                                long timeCost = travelTimeMatrixFinal[from][to] + serviceTimesFinal[from];

                                //set time weight to adjust how aggressive the optimizer is
                                return distanceCost + (long) (routingConfig.getTimeWeight() * timeCost);
                        });

                        routing.setArcCostEvaluatorOfAllVehicles(costCallbackIndex);
                } else {
                        routing.setArcCostEvaluatorOfAllVehicles(distanceCallbackIndex);
        }

        RoutingContext context = new RoutingContext(
                numVehicles,
                vehicleCapacities,
                demandCallbackIndex,
                routing,
                distanceCallbackIndex,
                timeCallbackIndex,
                manager,
                routingConfig,
                useTimes,
                timeWindows,
                depotStart,
                depotEnd,
                maxRouteDuration
        );
        addConstraints(context);

        RoutingSearchParameters searchParameters =
                configureSearchParams(routingConfig);

                Assignment solution;
                
                try {
                        solution = routing.solveWithParameters(searchParameters);
                } catch (Exception e) {
                        System.out.println("Error during solver execution: " + e.getMessage());
                        e.printStackTrace();
                        throw e;
                }

        Value status = routing.status();
        System.out.println("OR-Tools Solver Status: " + status);

        if (status == Value.ROUTING_INFEASIBLE) {
            System.out.println("[HINT] Solver reported INFEASIBLE. Try: \n" +
                    "  - Increase number of trucks or truck capacity\n" +
                    "  - Loosen time windows / increase wait slack\n" +
                    "  - Increase max route distance or max route duration\n" +
                    "  - Disable time constraints temporarily to isolate constraints");
            throw new NoSolutionFoundException(
                    "No feasible solution found. OR-Tools status: " + status +
                            ". Constraints are infeasible for the current inputs."
            );
        }

        if (solution != null) {
            return extractSolution(
                    solution,
                    numVehicles,
                    manager,
                    routing,
                    distanceMatrixCords,
                    demands
            );
        }

        if (status == Value.ROUTING_FAIL_TIMEOUT) {
            System.out.println("[HINT] Solver timed out after " + routingConfig.getCalculationTime() + "s. Try: \n" +
                    "  - Increase solve speed to BEST (longer time)\n" +
                    "  - Loosen time windows or increase wait slack\n" +
                    "  - Increase max route duration or add trucks\n" +
                    "  - Temporarily disable time constraints to check feasibility");
        }

        throw new NoSolutionFoundException(
                "No solution found. OR-Tools status: " + status +
                        (status == Value.ROUTING_FAIL_TIMEOUT ?
                                ". Timed out after " + routingConfig.getCalculationTime() + "s." : "")
        );
    }

    private RoutingSearchParameters configureSearchParams(
            RoutingConfig routingConfig
    ) {
        return main.defaultRoutingSearchParameters()
                .toBuilder()
                //PARALLEL_CHEAPEST_INSERTION
                .setFirstSolutionStrategy(
                        FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC
                )
                .setLocalSearchMetaheuristic(
                        LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH
                )
                .setLogSearch(false)
                .setTimeLimit(
                        Duration.newBuilder()
                                .setSeconds(
                                        routingConfig.getCalculationTime()
                                )
                                .build()
                )
                .build();
    }

    private void addConstraints(RoutingContext context) {

        // CAPACITY
        context.getRouting().addDimensionWithVehicleCapacity(
                context.getDemandCallbackIndex(),
                0,
                context.getVehicleCapacities(),
                true,
                "Capacity"
        );

        // DISTANCE
        context.getRouting().addDimension(
                context.getDistanceCallbackIndex(),
                0,
                context.getRoutingConfig().convertMilesToMeters(
                        context.getRoutingConfig().getMaxDistanceMiles()
                ),
                true,
                "Distance"
        );

        RoutingDimension distanceDimension =
                context.getRouting().getMutableDimension("Distance");

        for (int vehicleId = 0; vehicleId < context.getNumVehicles(); vehicleId++) {

        //     distanceDimension.setCumulVarSoftLowerBound(
        //             routing.end(vehicleId),
        //             routingConfig.convertMilesToMeters(
        //                     routingConfig.getOptimalDistanceMiles()
        //             ),
        //             routingConfig.convertMilesToMeters(
        //                     routingConfig.getPenaltyPerMileUnder()
        //             )
        //     );

        //     distanceDimension.setCumulVarSoftUpperBound(
        //             routing.end(vehicleId),
        //             routingConfig.convertMilesToMeters(
        //                     routingConfig.getOptimalDistanceMiles()
        //             ),
        //             routingConfig.convertMilesToMeters(
        //                     routingConfig.getPenaltyPerMileOver()
        //             )
        //     );

                //make each truck try to be a similar distance 
                distanceDimension.setGlobalSpanCostCoefficient(10);

            context.getRouting().setFixedCostOfVehicle(
                    context.getRoutingConfig().getCostOfAddingTruck(),
                    vehicleId
            );
        }

         
        // TIME DIMENSION - only if enabled
         
        if (context.isUseTimes()) {
            long waitSlack = context.getRoutingConfig().getTimeWindowSlackSeconds();
            System.out.println("Adding Time dimension: waitSlack=" + waitSlack + ", maxRouteDuration=" + context.getMaxRouteDuration());
            context.getRouting().addDimension(
                    context.getTimeCallbackIndex().intValue(),
                    // allowed waiting slack at stops
                    waitSlack,
                    context.getMaxRouteDuration(),  // max route duration
                    false,
                    "Time"
            );

            RoutingDimension timeDimension =
                    context.getRouting().getMutableDimension("Time");

            if (context.getTimeWindows() != null) {
                for (int location = 0; location < context.getTimeWindows().length; location++) {
                    long index = context.getManager().nodeToIndex(location);
                    timeDimension.cumulVar(index)
                            .setRange(
                                    context.getTimeWindows()[location][0],
                                    context.getTimeWindows()[location][1]
                            );
                    System.out.println("Set time window for node " + location + " -> [" + context.getTimeWindows()[location][0] + ", " + context.getTimeWindows()[location][1] + "]");
                }
            }

            for (int vehicleId = 0; vehicleId < context.getNumVehicles(); vehicleId++) {
                timeDimension.cumulVar(context.getRouting().start(vehicleId))
                        .setRange(context.getDepotStart(), context.getDepotEnd());
                timeDimension.cumulVar(context.getRouting().end(vehicleId))
                        .setRange(context.getDepotStart(), context.getDepotEnd());
                System.out.println("Vehicle " + vehicleId + " depot window -> [" + context.getDepotStart() + ", " + context.getDepotEnd() + "]");
            }
        }
    }

    private List<RouteResult> extractSolution(
            Assignment solution,
            int numVehicles,
            RoutingIndexManager manager,
            RoutingModel routing,
            long[][] distanceMatrixCords,
            int[] demands
    ) {

        List<RouteResult> results = new ArrayList<>();

        for (int vehicleId = 0; vehicleId < numVehicles; vehicleId++) {

            long index = routing.start(vehicleId);
            long nextIndex = solution.value(routing.nextVar(index));

            if (routing.isEnd(nextIndex)) {
                continue;
            }

            List<Integer> route = new ArrayList<>();
            long routeLoad = 0;
            long routeDistance = 0;

            long previousIndex = index;
            index = nextIndex;

            route.add(manager.indexToNode(previousIndex));

            while (!routing.isEnd(index)) {
                int nodeIndex = manager.indexToNode(index);
                routeLoad += demands[nodeIndex];
                route.add(nodeIndex);

                int fromNode = manager.indexToNode(previousIndex);
                int toNode = manager.indexToNode(index);
                routeDistance += distanceMatrixCords[fromNode][toNode];

                previousIndex = index;
                index = solution.value(routing.nextVar(index));
            }

            route.add(manager.indexToNode(index));

            int fromNode = manager.indexToNode(previousIndex);
            int toNode = manager.indexToNode(index);
            routeDistance += distanceMatrixCords[fromNode][toNode];

                        System.out.println("Vehicle " + vehicleId + " route: " + route + ", load=" + routeLoad + ", distance=" + routeDistance);
            results.add(
                    new RouteResult(
                            vehicleId,
                            route,
                            routeDistance,
                            routeLoad
                    )
            );
        }

        return results;
    }

    private void validateInputs(
            long[][] distanceMatrix,
            int[] demands,
            boolean useTimes,
            long[][] travelTimeMatrix,
            long[] serviceTimes,
            long[][] timeWindows,
            int numLocations
    ) {
        List<String> issues = new ArrayList<>();

        // Required: distanceMatrix and demands
        if (distanceMatrix == null) {
            issues.add("distanceMatrix is null");
        } else {
            if (distanceMatrix.length == 0 || distanceMatrix[0].length == 0) {
                issues.add("distanceMatrix has zero size");
            }
        }

        if (demands == null) {
            issues.add("demands is null");
        }

                // TODO: should probably validate in Service layer before even calling optimization,
                // except for validation only for this optimization method
                // Time inputs only validated when enabled
                if (useTimes) {
                        if (travelTimeMatrix == null) {
                                issues.add("travelTimeMatrixSeconds is null but useTimeConstraints=true");
                        }
                        if (distanceMatrix != null && distanceMatrix.length > 0) {
                                int n = distanceMatrix.length;
                                if (demands != null && demands.length != n) {
                                        issues.add("demands length " + demands.length + " != numLocations " + n);
                                }
                                if (travelTimeMatrix != null && travelTimeMatrix.length != n) {
                                        issues.add("travelTimeMatrix rows " + travelTimeMatrix.length + " != numLocations " + n);
                                }
                                if (serviceTimes != null && serviceTimes.length != n) {
                                        issues.add("serviceTimes length " + serviceTimes.length + " != numLocations " + n);
                                }
                                if (timeWindows != null && timeWindows.length != n) {
                                        issues.add("timeWindows rows " + timeWindows.length + " != numLocations " + n);
                                }
                        }
                } else {
                        if (distanceMatrix != null && distanceMatrix.length > 0) {
                                int n = distanceMatrix.length;
                                if (demands != null && demands.length != n) {
                                        issues.add("demands length " + demands.length + " != numLocations " + n);
                                }
                        }
                }

        if (!issues.isEmpty()) {
            System.out.println("[DEBUG] Input validation failed:");
            for (String issue : issues) {
                System.out.println("  - " + issue);
            }
            throw new IllegalArgumentException("Invalid routing inputs: " + String.join(", ", issues));
        }
    }

}
