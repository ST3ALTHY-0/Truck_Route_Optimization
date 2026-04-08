package com.truckoptimization.task.optimizeRoute;

import java.util.List;

import org.springframework.stereotype.Service;

import com.truckoptimization.dto.location.Location;
import com.truckoptimization.dto.waypoint.Waypoint;

@Service
public class MatrixResolutionService {

    private final MatrixCacheService matrixCacheService;

    public MatrixResolutionService(MatrixCacheService matrixCacheService) {
        this.matrixCacheService = matrixCacheService;
    }

    public MatrixData resolveMatrices(List<Waypoint> waypoints, List<Location> locations) {
        return matrixCacheService.getOrFetch(waypoints, locations);
    }
}
