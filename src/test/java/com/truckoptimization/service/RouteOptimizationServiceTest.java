// package com.truckoptimization.service;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.DisplayName;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import com.truckoptimization.config.RoutingConfig;
// import com.truckoptimization.exception.DepotDemandException;
// import com.truckoptimization.model.CompiledResults;
// import com.truckoptimization.model.RouteResult;
// import com.truckoptimization.model.location.Location;
// import com.truckoptimization.model.location.LocationService;
// import com.truckoptimization.service.optimizeRoute.OptimizeRoutesWithORTools;

// @DisplayName("RouteOptimizationService Tests")
// class RouteOptimizationServiceTest {

//     private RouteOptimizationService routeOptimizationService;

//     @Mock
//     private CsvParserService csvParserService;

//     @Mock
//     private DistanceToLatLongConvertor distanceToLatLongConvertor;

//     @Mock
//     private DistanceMatrixCalculation distanceMatrixCalculation;

//     @Mock
//     private OptimizeRoutesWithORTools optimizeRoutesWithORTools;

//     @Mock
//     private LocationService locationService;

//     private RoutingConfig routingConfig;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         routeOptimizationService = new RouteOptimizationService(
//                 csvParserService,
//                 distanceToLatLongConvertor,
//                 distanceMatrixCalculation,
//                 optimizeRoutesWithORTools,
//                 locationService
//         );

//         // Initialize routing config
//         routingConfig = new RoutingConfig();
//         routingConfig.setNumberOfTrucks(2);
//         routingConfig.setTruckCapacity(10);
//         routingConfig.setMaxDistanceMiles(100);
//         routingConfig.setOptimalDistanceMiles(50);
//         routingConfig.setCalculationTime(30);
//         routingConfig.setPenaltyPerMileOver(5);
//         routingConfig.setPenaltyPerMileUnder(2);
//         routingConfig.setCostOfAddingTruck(100);
//     }

//     @Test
//     @DisplayName("Should successfully optimize routes with valid addresses and demands")
//     void testOptimizeRoutesWithValidAddressesAndDemands() throws Exception {
//         // Arrange
//         List<String> addresses = Arrays.asList(
//                 "INDIANAPOLIS, IN",
//                 "123 Main St, Indianapolis, IN",
//                 "456 Oak Ave, Indianapolis, IN"
//         );
//         List<Integer> demands = Arrays.asList(0, 5, 3);

//         // Create mock locations with coordinates
//         Location depot = createMockLocation("INDIANAPOLIS, IN", 39.7684, -86.1581);
//         Location location1 = createMockLocation("Indianapolis, IN", 39.762821, -86.396568);
//         Location location2 = createMockLocation("Cleveland, Ohio", 41.499321, -81.694359);
//         Location location3 = createMockLocation("kalamazoo, Michigan", 42.289777, -85.584682);

//         // Mock distance matrix
//         long[][] distanceMatrix = new long[][] {
//                 { 0, 5000, 10000 },
//                 { 5000, 0, 6000 },
//                 { 10000, 6000, 0 }
//         };

//         // Mock travel time matrix
//         long[][] travelTimeMatrix = new long[][] {
//                 { 0, 300, 600 },
//                 { 300, 0, 360 },
//                 { 600, 360, 0 }
//         };

//         // Mock route result
//         List<RouteResult> mockRouteResults = Arrays.asList(
//                 new RouteResult(0, Arrays.asList(0, 1, 2), new int[] { 0, 5, 3 }, 15000, 8)
//         );

//         // Setup mock behaviors
//         when(distanceToLatLongConvertor.convertAddressToLatLong(anyString()))
//                 .thenReturn(depot);
//         when(distanceMatrixCalculation.getDistanceMatrixInBatches(any()))
//                 .thenReturn(distanceMatrix);
//         when(distanceMatrixCalculation.getTravelTimeMatrixInBatches(any()))
//                 .thenReturn(travelTimeMatrix);
//         when(optimizeRoutesWithORTools.optimizeRoutes(eq(distanceMatrix), any(), any()))
//                 .thenReturn(mockRouteResults);

//         // Act
//         CompiledResults result = routeOptimizationService.optimizeRoutesWithAddressAndDemands(
//                 addresses,
//                 demands,
//                 routingConfig
//         );

//         // Assert
//         assertNotNull(result, "CompiledResults should not be null");
//         assertNotNull(result.getResults(), "Route results should not be null");
//         assertEquals(1, result.getResults().size(), "Should have 1 route");
//         assertEquals(distanceMatrix, result.getMatrix(), "Distance matrix should match");
//         assertEquals(travelTimeMatrix, result.getTravelTimeMatrix(), "Travel time matrix should match");

//         // Verify mocks were called
//         verify(distanceMatrixCalculation, times(1)).getDistanceMatrixInBatches(any());
//         verify(distanceMatrixCalculation, times(1)).getTravelTimeMatrixInBatches(any());
//         verify(optimizeRoutesWithORTools, times(1)).optimizeRoutes(any(), any(), any());
//     }

//     @Test
//     @DisplayName("Should throw DepotDemandException when first demand is not 0")
//     void testThrowsExceptionWhenFirstDemandNotZero() {
//         // Arrange
//         List<String> addresses = Arrays.asList(
//                 "INDIANAPOLIS, IN",
//                 "123 Main St, Indianapolis, IN"
//         );
//         List<Integer> demands = Arrays.asList(5, 3); // First demand should be 0

//         // Act & Assert
//         assertThrows(DepotDemandException.class, () -> {
//             routeOptimizationService.optimizeRoutesWithAddressAndDemands(
//                     addresses,
//                     demands,
//                     routingConfig
//             );
//         }, "Should throw DepotDemandException when first demand is not 0");
//     }

//     @Test
//     @DisplayName("Should throw IllegalArgumentException when addresses and demands size mismatch")
//     void testThrowsExceptionWhenAddressesAndDemandsSizeMismatch() {
//         // Arrange
//         List<String> addresses = Arrays.asList(
//                 "INDIANAPOLIS, IN",
//                 "123 Main St, Indianapolis, IN"
//         );
//         List<Integer> demands = Arrays.asList(0, 5, 3); // More demands than addresses

//         // Act & Assert
//         assertThrows(IllegalArgumentException.class, () -> {
//             routeOptimizationService.optimizeRoutesWithAddressAndDemands(
//                     addresses,
//                     demands,
//                     routingConfig
//             );
//         }, "Should throw IllegalArgumentException when sizes don't match");
//     }

//     @Test
//     @DisplayName("Should return null when no routes are found")
//     void testReturnsNullWhenNoRoutesFound() throws Exception {
//         // Arrange
//         List<String> addresses = Arrays.asList(
//                 "INDIANAPOLIS, IN",
//                 "123 Main St, Indianapolis, IN"
//         );
//         List<Integer> demands = Arrays.asList(0, 5);

//         Location depot = createMockLocation("INDIANAPOLIS, IN", 39.7684, -86.1581);
//         long[][] distanceMatrix = new long[][] {
//                 { 0, 5000 },
//                 { 5000, 0 }
//         };
//         long[][] travelTimeMatrix = new long[][] {
//                 { 0, 300 },
//                 { 300, 0 }
//         };

//         when(distanceToLatLongConvertor.convertAddressToLatLong(anyString()))
//                 .thenReturn(depot);
//         when(distanceMatrixCalculation.getDistanceMatrixInBatches(any()))
//                 .thenReturn(distanceMatrix);
//         when(distanceMatrixCalculation.getTravelTimeMatrixInBatches(any()))
//                 .thenReturn(travelTimeMatrix);
//         when(optimizeRoutesWithORTools.optimizeRoutes(eq(distanceMatrix), any(), any()))
//                 .thenReturn(new ArrayList<>()); // Return empty list

//         // Act
//         CompiledResults result = routeOptimizationService.optimizeRoutesWithAddressAndDemands(
//                 addresses,
//                 demands,
//                 routingConfig
//         );

//         // Assert
//         assertNull(result, "Should return null when no routes are found");
//     }

//     @Test
//     @DisplayName("Should handle multiple routes correctly")
//     void testHandlesMultipleRoutesCorrectly() throws Exception {
//         // Arrange
//         List<String> addresses = Arrays.asList(
//                 "INDIANAPOLIS, IN",
//                 "123 Main St, Indianapolis, IN",
//                 "456 Oak Ave, Indianapolis, IN",
//                 "789 Pine Rd, Indianapolis, IN"
//         );
//         List<Integer> demands = Arrays.asList(0, 5, 3, 4);

//         Location depot = createMockLocation("INDIANAPOLIS, IN", 39.7684, -86.1581);
//         long[][] distanceMatrix = new long[][] {
//                 { 0, 5000, 10000, 15000 },
//                 { 5000, 0, 6000, 8000 },
//                 { 10000, 6000, 0, 7000 },
//                 { 15000, 8000, 7000, 0 }
//         };
//         long[][] travelTimeMatrix = new long[][] {
//                 { 0, 300, 600, 900 },
//                 { 300, 0, 360, 480 },
//                 { 600, 360, 0, 420 },
//                 { 900, 480, 420, 0 }
//         };

//         List<RouteResult> mockRouteResults = Arrays.asList(
//                 new RouteResult(0, Arrays.asList(0, 1, 2), new int[] { 0, 5, 3 }, 15000, 8),
//                 new RouteResult(1, Arrays.asList(0, 3), new int[] { 0, 4 }, 15000, 4)
//         );

//         when(distanceToLatLongConvertor.convertAddressToLatLong(anyString()))
//                 .thenReturn(depot);
//         when(distanceMatrixCalculation.getDistanceMatrixInBatches(any()))
//                 .thenReturn(distanceMatrix);
//         when(distanceMatrixCalculation.getTravelTimeMatrixInBatches(any()))
//                 .thenReturn(travelTimeMatrix);
//         when(optimizeRoutesWithORTools.optimizeRoutes(eq(distanceMatrix), any(), any()))
//                 .thenReturn(mockRouteResults);

//         // Act
//         CompiledResults result = routeOptimizationService.optimizeRoutesWithAddressAndDemands(
//                 addresses,
//                 demands,
//                 routingConfig
//         );

//         // Assert
//         assertNotNull(result, "CompiledResults should not be null");
//         assertEquals(2, result.getResults().size(), "Should have 2 routes");
//         assertEquals(distanceMatrix, result.getMatrix(), "Distance matrix should match");
//         assertEquals(travelTimeMatrix, result.getTravelTimeMatrix(), "Travel time matrix should match");
//     }

//     @Test
//     @DisplayName("Should set routing config with travel time matrix")
//     void testSetsRoutingConfigWithTravelTimeMatrix() throws Exception {
//         // Arrange
//         List<String> addresses = Arrays.asList(
//                 "INDIANAPOLIS, IN",
//                 "123 Main St, Indianapolis, IN"
//         );
//         List<Integer> demands = Arrays.asList(0, 5);

//         Location depot = createMockLocation("INDIANAPOLIS, IN", 39.7684, -86.1581);
//         long[][] distanceMatrix = new long[][] {
//                 { 0, 5000 },
//                 { 5000, 0 }
//         };
//         long[][] travelTimeMatrix = new long[][] {
//                 { 0, 300 },
//                 { 300, 0 }
//         };

//         List<RouteResult> mockRouteResults = Arrays.asList(
//                 new RouteResult(0, Arrays.asList(0, 1), new int[] { 0, 5 }, 5000, 5)
//         );

//         when(distanceToLatLongConvertor.convertAddressToLatLong(anyString()))
//                 .thenReturn(depot);
//         when(distanceMatrixCalculation.getDistanceMatrixInBatches(any()))
//                 .thenReturn(distanceMatrix);
//         when(distanceMatrixCalculation.getTravelTimeMatrixInBatches(any()))
//                 .thenReturn(travelTimeMatrix);
//         when(optimizeRoutesWithORTools.optimizeRoutes(eq(distanceMatrix), any(), any()))
//                 .thenReturn(mockRouteResults);

//         // Act
//         CompiledResults result = routeOptimizationService.optimizeRoutesWithAddressAndDemands(
//                 addresses,
//                 demands,
//                 routingConfig
//         );

//         // Assert
//         assertNotNull(result);
//         assertEquals(travelTimeMatrix, routingConfig.getTravelTimeMatrixSeconds(),
//                 "Routing config should have travel time matrix set");
//     }

//     // Helper method to create mock locations
//     private Location createMockLocation(String address, double latitude, double longitude) {
//         Location location = new Location();
//         location.setAddress(address);
//         location.setLatitude(latitude);
//         location.setLongitude(longitude);
//         return location;
//     }
// }
