package com.truckoptimization.task.optimizeRoute;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.truckoptimization.dto.database.nosql.feature.matrixCache.MatrixDocument;
import com.truckoptimization.dto.database.nosql.feature.matrixCache.MatrixService;
import com.truckoptimization.dto.location.Location;
import com.truckoptimization.dto.waypoint.Waypoint;
import com.truckoptimization.task.api.orsApi.DistanceMatrixCalculation;

@Service
public class MatrixCacheService {

    private final MatrixService matrixService;
    private final DistanceMatrixCalculation distanceMatrixCalculation;

    public MatrixCacheService(MatrixService matrixService,
            DistanceMatrixCalculation distanceMatrixCalculation) {
        this.matrixService = matrixService;
        this.distanceMatrixCalculation = distanceMatrixCalculation;
    }

    public MatrixData getOrFetch(List<Waypoint> waypoints, List<Location> locations) {
        Optional<MatrixDocument> matrixDocument = matrixService.findByWaypointsHash(waypoints.hashCode());
        if (matrixDocument.isPresent()) {
            MatrixDocument cached = matrixDocument.get();
            if (cached.getDistanceMatrix() != null && cached.getTravelTimeMatrix() != null) {
                return new MatrixData(cached.getDistanceMatrix(), cached.getTravelTimeMatrix());
            }
        }

        List<double[]> coordinates = locations.stream()
                .map(loc -> new double[] { loc.getLatitude(), loc.getLongitude() })
                .collect(Collectors.toList());

        DistanceMatrixCalculation.DistanceAndTimeMatrices matrices =
                distanceMatrixCalculation.getDistanceAndTravelTimeMatricesInBatches(coordinates, locations);

        MatrixDocument document = matrixDocument.orElseGet(MatrixDocument::new);
        document.setWaypoints(waypoints);
        document.setDistanceMatrix(matrices.distanceMatrix);
        document.setTravelTimeMatrix(matrices.timeMatrix);
        matrixService.saveDocument(document);

        return new MatrixData(matrices.distanceMatrix, matrices.timeMatrix);
    }
}