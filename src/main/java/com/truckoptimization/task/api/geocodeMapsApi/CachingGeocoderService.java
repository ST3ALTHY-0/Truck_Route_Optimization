package com.truckoptimization.task.api.geocodeMapsApi;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.truckoptimization.dto.location.Location;
import com.truckoptimization.dto.location.LocationService;
import com.truckoptimization.exception.CoordsApiException;

@Service
public class CachingGeocoderService {

    private final LocationService locationService;
    private final GeocodingService geocodingService;

    public CachingGeocoderService(LocationService locationService, GeocodingService geocodingService) {
        this.locationService = locationService;
        this.geocodingService = geocodingService;
    }

    public Location convertAddressToLatLong(String address) {
        Optional<Location> existingLocation = locationService.checkLocationExists(address);

        if (existingLocation.isPresent()) {
            Location location = existingLocation.get();
            if (location.getLatitude() != null && location.getLongitude() != null) {
                System.out.println("Coords exist of: " + address);
                return location;
            }
            throw new CoordsApiException("API error: Location has null coordinates for address: " + address);
        }

        Location resolvedLocation = geocodingService.convertAddressToLatLong(address);
        return locationService.saveLocation(address, resolvedLocation.getLatitude(), resolvedLocation.getLongitude());
    }
}
