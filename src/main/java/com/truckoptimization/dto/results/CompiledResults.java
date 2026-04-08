package com.truckoptimization.dto.results;

import java.util.List;

import com.truckoptimization.dto.location.Location;

import lombok.Data;

@Data
public class CompiledResults {

    private List<RouteResult> results;
    private long[][] matrix;
    private long[][] travelTimeMatrix;
    private String matrixHash;
    private List<Location> locations;
    private int[] demand;

    public void printRouteResults() {
        System.out.println("Total vehicles used: " + results.size());
        for (RouteResult result : results) {
            System.out.println("Route for Vehicle " + result.getVehicleId() + ":");
            System.out.print("  Route: ");
            for (int idx : result.getRoute()) {
                    System.out.print("[" + idx + ": " + locations.get(idx).getAddress() + "] ");
            }
            System.out.println();
            System.out.println("  Total Distance: " + result.getTotalDistance() + " meters");
            System.out.println("  Total Load: " + result.getTotalLoad());
            System.out.println();
        }
    }

    public List<double[]> getRouteCoordinates(List<Integer> route) {
        System.out.println("[DEBUG] getRouteCoordinates called with route: " + route);
        System.out.println("[DEBUG] locations.size() = " + locations.size());
        
        return route.stream()
                .map(idx -> {
                    if (idx < 0 || idx >= locations.size()) {
                        System.out.println("[WARN] Route index " + idx + " out of bounds for locations list (size=" + locations.size() + ")");
                        // Skip this index or provide a fallback
                        return null;
                    }
                    Location loc = locations.get(idx);
                    System.out.println("[DEBUG] Route index " + idx + " -> " + loc.getAddress() + " [" + loc.getLatitude() + ", " + loc.getLongitude() + "]");
                    return new double[]{loc.getLatitude(), loc.getLongitude()};
                })
                .filter(coord -> coord != null)  // Filter out nulls from out-of-bounds indices
                .toList();
    }
    
}
