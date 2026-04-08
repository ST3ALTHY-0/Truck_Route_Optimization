package com.truckoptimization.dto.database.sql.features.location;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationEntityService {

    @Autowired
    private LocationEntityRepository locationRepository;

    public LocationEntity getOrSaveLocation(String address, double lat, double lng) {
        return locationRepository.findByAddress(address)
                .orElseGet(() -> {
                    LocationEntity loc = new LocationEntity();
                    loc.setAddress(address);
                    loc.setLatitude(lat);
                    loc.setLongitude(lng);
                    return locationRepository.save(loc);
                });

    }

    public Optional<LocationEntity> checkLocationExists(String address) {
        return locationRepository.findByAddress(address);
    }

    public LocationEntity saveLocation(String address, double lat, double lng) {
        LocationEntity loc = new LocationEntity();
        loc.setAddress(address);
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        return locationRepository.save(loc);
    }

}
