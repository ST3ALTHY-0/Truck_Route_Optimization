package com.truckoptimization.task.optimizeRoute;

import java.util.List;

import org.springframework.stereotype.Service;

import com.truckoptimization.dto.location.Location;
import com.truckoptimization.exception.DepotDemandException;
import com.truckoptimization.exception.InvalidInputException;

@Service
public class RouteValidationService {

    public void validateWaypointInput(List<String> addresses, List<Integer> demands) {
        if (addresses.size() != demands.size()) {
            throw new IllegalArgumentException("Addresses and demands lists must have the same size.");
        }
        if (demands.get(0) != 0) {
            throw new DepotDemandException("First demand must be set to 0 to set the depo");
        }
    }

    public void validateFurthestLocationReachability(long[][] distanceMatrix, List<Location> locations,
            long maxDistanceMeters, double maxDistanceMiles) {
        if (distanceMatrix == null || distanceMatrix.length <= 1) {
            return;
        }

        int depotIndex = 0;
        long furthestOneWayDistanceMeters = -1;
        int furthestLocationIndex = -1;

        for (int i = 1; i < distanceMatrix[depotIndex].length; i++) {
            long distanceFromDepot = distanceMatrix[depotIndex][i];
            if (distanceFromDepot > furthestOneWayDistanceMeters) {
                furthestOneWayDistanceMeters = distanceFromDepot;
                furthestLocationIndex = i;
            }
        }

        if (furthestOneWayDistanceMeters < 0 || furthestOneWayDistanceMeters == Long.MAX_VALUE) {
            throw new InvalidInputException(
                    "Could not validate route distance limits because one or more locations are unreachable from the depot.");
        }

        long requiredRoundTripMeters = furthestOneWayDistanceMeters * 2L;
        if (requiredRoundTripMeters > maxDistanceMeters) {
            Location furthestLocation = locations.get(furthestLocationIndex);
            throw new InvalidInputException(
                    String.format(
                            "Invalid input: location '%s' is too far from the depot for the configured max truck distance %.2f miles. At least %.2f miles is required for a depot round trip.",
                            furthestLocation.getAddress(),
                            maxDistanceMiles,
                            requiredRoundTripMeters / 1609.344));
        }
    }
}
