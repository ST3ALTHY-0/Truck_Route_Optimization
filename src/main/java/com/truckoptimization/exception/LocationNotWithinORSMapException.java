package com.truckoptimization.exception;

import java.util.List;

public class LocationNotWithinORSMapException extends RuntimeException {

    private List<LocationInfo> unmappedLocations;

    public LocationNotWithinORSMapException(String message, List<LocationInfo> unmappedLocations) {
        super(message);
        this.unmappedLocations = unmappedLocations;
        // Print diagnostic info immediately when exception is created
        printDiagnostics();
    }

    public List<LocationInfo> getUnmappedLocations() {
        return unmappedLocations;
    }

    private void printDiagnostics() {
        System.err.println("ERROR: ORS Map Coverage Issue Detected");
        System.err.println("The following locations are NOT within the ORS map data:");
        for (LocationInfo loc : unmappedLocations) {
            System.err.println("  " + loc.toString());
        }
        System.err.println("");
        System.err.println("Possible causes:");
        System.err.println("  1. ORS container has limited map data coverage");
        System.err.println("  2. Locations exceed maximum_search_radius in ors-config.yml");
        System.err.println("  3. OSM data doesn't include these regions");
        System.err.println("");
        System.err.println("Solutions:");
        System.err.println("  - Load complete US/regional map data into ORS container");
        System.err.println("  - Increase maximum_search_radius in ors-config.yml");
        System.err.println("  - Use fallback Haversine distance calculation for these routes");
    }

    /**
     * Holds information about a location that ORS couldn't map
     */
    public static class LocationInfo {
        private int index;
        private double latitude;
        private double longitude;
        private String address;

        public LocationInfo(int index, double latitude, double longitude, String address) {
            this.index = index;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
        }

        public int getIndex() {
            return index;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getAddress() {
            return address;
        }

        @Override
        public String toString() {
            return String.format("Location %d: %s [lat=%.6f, lon=%.6f]", 
                index, address != null ? address : "Unknown", latitude, longitude);
        }
    }
}
