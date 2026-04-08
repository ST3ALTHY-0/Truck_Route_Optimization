package com.truckoptimization.task.controller.support;

import java.util.List;

import org.springframework.stereotype.Service;

import com.truckoptimization.dto.direction.RouteApiResponse;
import com.truckoptimization.dto.results.CompiledResults;
import com.truckoptimization.dto.results.RouteResult;
import com.truckoptimization.task.api.orsApi.DirectionService;

@Service
public class RouteGeometryService {

    private final DirectionService directionService;

    public RouteGeometryService(DirectionService directionService) {
        this.directionService = directionService;
    }

    public CompiledResults addGeometry(CompiledResults result) {
        List<RouteResult> routeResults = result.getResults();

        for (int index = 0; index < routeResults.size(); index++) {
            RouteResult routeResult = routeResults.get(index);
            List<double[]> coords = result.getRouteCoordinates(routeResult.getRoute());
            RouteApiResponse routes = directionService.getDirections(coords);
            if (routes != null && routes.getRoutes() != null && routes.getRoutes().size() > 0) {
            directionService.addGeometryToRouteResult(routes.getRoutes().get(0), routeResult);
            //System.out.println("[DEBUG] Route " + (index + 1) + " encodedGeometry set to: " + routeResult.getEncodedGeometry());
            } else {
            System.out.println("[WARN] No routes returned from ORS API for route " + (index + 1));
            }
        }

        return result;
    }
}
