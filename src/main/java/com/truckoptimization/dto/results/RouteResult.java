package com.truckoptimization.dto.results;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@AllArgsConstructor
public class RouteResult {
    @Id
    private int vehicleId;
    private List<Integer> route;
    // private int[] demand;
    private long totalDistance;
    private long totalLoad;
    private String encodedGeometry; //for the encoded polyline we get for the route

    public RouteResult(int vehicleId, List<Integer> route, long totalDistance, long totalLoad) {
        this.vehicleId = vehicleId;
        this.route = route;
        this.totalDistance = totalDistance;
        this.totalLoad = totalLoad;
    }
    
    
    static void printRouteResults(List<RouteResult> results, long[][] matrix, List<String> addresses) {
        System.out.println("Total vehicles used: " + results.size());
        for (RouteResult result : results) {
            System.out.println("Route for Vehicle " + result.getVehicleId() + ":");
            System.out.print("  Route: ");
            for (int idx : result.getRoute()) {
                if (idx == 0) {
                    System.out.print("[" + idx + ": " + addresses.get(idx) + "] ");

                } else {
                    System.out.print("[" + idx + ": " + addresses.get(idx + 1) + "] ");

                }
            }
            System.out.println();
            System.out.println("  Total Distance: " + result.getTotalDistance() + " meters");
            System.out.println("  Total Load: " + result.getTotalLoad());
            System.out.println();
        }
    }


}