package com.truckoptimization.task.optimizeRoute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.truckoptimization.common.config.RoutingConfig;
import com.truckoptimization.dto.location.Location;
import com.truckoptimization.dto.location.LocationService;
import com.truckoptimization.dto.results.CompiledResults;
import com.truckoptimization.dto.results.RouteResult;
import com.truckoptimization.dto.waypoint.Waypoint;
import com.truckoptimization.task.csvParser.ComplexCsvParserService;

/*
Note: If anyone is reading this code, im sorry,
I took a half a year break before coming back to it
and even I don't understand wtf I was doing in some parts
 */
@Service
public class RouteOptimizationService {

    private final ComplexCsvParserService csvParserService;
    private final com.truckoptimization.task.api.geocodeMapsApi.CachingGeocoderService cachingGeocoderService;
    private final MatrixResolutionService matrixResolutionService;
    private final RouteValidationService routeValidationService;
    private final OptimizeRoutesWithORTools optimizeRoutesWithORTools;
    private final LocationService locationService;

    public RouteOptimizationService(
            ComplexCsvParserService csvParserService,
            com.truckoptimization.task.api.geocodeMapsApi.CachingGeocoderService cachingGeocoderService,
            MatrixResolutionService matrixResolutionService,
            RouteValidationService routeValidationService,
            OptimizeRoutesWithORTools optimizeRoutesWithORTools,
            LocationService locationService) {
        this.csvParserService = csvParserService;
        this.cachingGeocoderService = cachingGeocoderService;
        this.matrixResolutionService = matrixResolutionService;
        this.routeValidationService = routeValidationService;
        this.optimizeRoutesWithORTools = optimizeRoutesWithORTools;
        this.locationService = locationService;
    }

    // IMPORTANT: this is the used method for the web: focus on this one
    public CompiledResults optimizeRoutesWithWaypoints(List<Waypoint> waypoints,
            RoutingConfig routingConfig) {
         List<String> addresses = waypoints.stream().map(Waypoint::getAddress).collect(Collectors.toList());
        List<Integer> demands = waypoints.stream().map(Waypoint::getDemand).collect(Collectors.toList());        

        routeValidationService.validateWaypointInput(addresses, demands);

        // we make the compiled results a list because I wanted to be able to handle multiple different
        // routes at once for the csv, but for the web we only do one at a time, so we make a list and return the first result for the web
        // stupid.
        List<CompiledResults> results = Collections.synchronizedList(new ArrayList<>());

        List<Location> locations = addresses.stream()
                .map(address -> {
                    Location loc = new Location();
                    loc.setAddress(address);
                    return loc;
                })
                .collect(Collectors.toList());

        for (int i = 0; i < locations.size(); i++) {
            locations.set(i, checkCoordsExist(locations.get(i)));
        }

        int[] demandArray = demands.stream().mapToInt(Integer::intValue).toArray();

        MatrixData matrices = matrixResolutionService.resolveMatrices(waypoints, locations);
        routingConfig.setTravelTimeMatrixSeconds(matrices.getTravelTimeMatrix());
        routingConfig.setDistanceMatrix(matrices.getDistanceMatrix());
        
        
        long maxDistanceMeters = routingConfig.convertMilesToMeters(routingConfig.getMaxDistanceMiles());
        routeValidationService.validateFurthestLocationReachability(routingConfig.getDistanceMatrix(), locations, maxDistanceMeters,
                routingConfig.getMaxDistanceMiles());
        
        

        //we update the results object inside the processRoute method
        processRoute(locations, demandArray, results, routingConfig);
        if (results.isEmpty()) {
            return null;
        } else if (results.get(0) == null) {
            return null;
        }
        return results.get(0);

    }

    // maybe we should do this in the controller class before we call
    // OptimizeRoutesService
    private Location checkCoordsExist(Location location) {
        if (location.getLatitude() == null || location.getLongitude() == null) {
            location = cachingGeocoderService
                    .convertAddressToLatLong(location.getAddress());
        }
        return location;
    }

    private void processRoute(List<Location> locations, int[] demands,
            List<CompiledResults> allResults, RoutingConfig routingConfig) {

        List<RouteResult> results = optimizeRoutesWithORTools.optimizeRoutes(routingConfig.getDistanceMatrix(), demands,
                routingConfig);

        if (results != null && !results.isEmpty()) {
            CompiledResults compiledResults = new CompiledResults();
            compiledResults.setResults(results);
            compiledResults.setMatrix(routingConfig.getDistanceMatrix());
            compiledResults.setTravelTimeMatrix(routingConfig.getTravelTimeMatrixSeconds()); // store travel times too
            compiledResults.setLocations(locations);
            compiledResults.setDemand(demands);
            allResults.add(compiledResults);
        }
    }

}
