package com.truckoptimization.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO for truck route optimization request containing all parameters from the UI form.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizationRequest {
    
    private String manualInput;
    
    
    // Fleet Parameters
    private Integer numberOfTrucks;
    private Integer truckCapacity;
    private Long maxDistanceMiles;
    private Long optimalDistanceMiles;
    
    // Optimization Parameters
    private Integer calculationTime;
    private Long penaltyPerMileOver;
    private Long penaltyPerMileUnder;
    private Long costOfAddingTruck;
    
    // Route Options
    private String routeType;
    private LocalDate departDate;
    private LocalTime departTime;
    
    private Boolean avoidTollRoads;
    private Boolean reorderStops;
    private Boolean avoidFerries;
    private Boolean avoidTunnels;
    private Boolean avoidDirtRoads;
    private Boolean considerTraffic;
    private Boolean avoidSharpTurns;
    private Boolean applySeasonalClosures;
    
    // Truck Parameters (per truck, stored as JSON or map)
    private List<TruckParameters> truckParameters;
    

    /**
     * Nested class for truck-specific parameters
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TruckParameters {
        private Integer truckNumber;
        
        // Vehicle info
        private String type; 
        private Integer axleCount;
        private String hazmat; 
        
        // Cost parameters
        private Double fixedCost;
        private Double costPerMile;
        private Double costPerHour;
        
        // Dimensions (feet and inches)
        private Integer heightFeet;
        private Integer heightInches;
        private Integer lengthFeet;
        private Integer lengthInches;
        private Integer widthFeet;
        private Integer widthInches;
        
        // Weight and performance
        private Integer weight; 
        private Integer maxSpeed; 
        
        // Fuel
        private String fuel; 
        private Double mpg;
        private Double dollarPerGallon;
        
        // Working hours
        private String workingTimeType; 
        private Double workingHours; 
        private LocalTime workingStartTime; 
        private LocalTime workingEndTime; 
    }
}
