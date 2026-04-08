package com.truckoptimization.task.api.orsApi;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.truckoptimization.dto.location.Location;
import com.truckoptimization.exception.ApiNotConnectedException;
import com.truckoptimization.exception.LocationNotWithinORSMapException;
import com.truckoptimization.exception.ORSApiException;
import com.truckoptimization.common.util.ApiKeyUtil;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.security.MessageDigest;
import java.util.Arrays;

//helper class to calculate the distance matrix (how far away each location is from all other locations). We can use our 'own' algorithems to find the solution,
//which is not great because they lack precision, only accounting for straight distance, not roads; and we can use a free api to calculate this for us, limits of 500 requests per day. 


//Could def separate the time/distance matrixes into their own classes
@Service
public class DistanceMatrixCalculation {

    private static final String MATRIX_URL = "http://localhost:8090/ors/v2/matrix/driving-hgv";
    private final String orsApiKey;
    private final Duration orsRequestTimeout;
    private final HttpClient httpClient;

    public DistanceMatrixCalculation(
            @Value("${ors.api.key:}") String orsApiKey,
            @Value("${ors.api.timeout-seconds:20}") long orsTimeoutSeconds) {
        this.orsApiKey = orsApiKey;
        this.orsRequestTimeout = Duration.ofSeconds(Math.max(1, orsTimeoutSeconds));
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(this.orsRequestTimeout)
                .build();
    }

    /**
     * Inner class to hold ORS API response with distance and/or duration data
     */
    public static class ORSMatrixResponse {
        private JSONObject responseJson;
        private int numSources;
        private int numDestinations;

        public ORSMatrixResponse(JSONObject responseJson, int numSources, int numDestinations) {
            this.responseJson = responseJson;
            this.numSources = numSources;
            this.numDestinations = numDestinations;
        }

        public long[][] getDistances() {
            if (!responseJson.has("distances")) {
                return null;
            }
            JSONArray distances = responseJson.getJSONArray("distances");
            long[][] matrix = new long[numSources][numDestinations];
            for (int i = 0; i < numSources; i++) {
                JSONArray row = distances.getJSONArray(i);
                for (int j = 0; j < numDestinations; j++) {
                    matrix[i][j] = row.isNull(j) ? Long.MAX_VALUE : row.getLong(j);
                }
            }
            return matrix;
        }

        public long[][] getDurations() {
            if (!responseJson.has("durations")) {
                return null;
            }
            JSONArray durations = responseJson.getJSONArray("durations");
            long[][] matrix = new long[numSources][numDestinations];
            for (int i = 0; i < numSources; i++) {
                JSONArray row = durations.getJSONArray(i);
                for (int j = 0; j < numDestinations; j++) {
                    matrix[i][j] = row.isNull(j) ? Long.MAX_VALUE : row.getLong(j);
                }
            }
            return matrix;
        }
    }

    /**
     * Inner class to hold both distance and duration matrices from a single API request
     */
    public static class DistanceAndTimeMatrices {
        public long[][] distanceMatrix;
        public long[][] timeMatrix;

        public DistanceAndTimeMatrices(long[][] distanceMatrix, long[][] timeMatrix) {
            this.distanceMatrix = distanceMatrix;
            this.timeMatrix = timeMatrix;
        }
    }

    private String requireOrsApiKey() {
        return ApiKeyUtil.requireApiKey(orsApiKey, "ORS_API_KEY");
    }
    /**
     * Sends coordinates to OpenRouteService and retrieves a distance matrix.
     *
     * @param coordinates A list of [latitude, longitude] arrays.
     * @return A 2D long[][] distance matrix in meters.
     * @throws Exception if the HTTP request fails or the response is invalid.
     */

    public long[][] getDistanceMatrix(List<double[]> coordinates) throws Exception {
        int size = coordinates.size();
        long[][] matrix = new long[size][size];

        JSONArray locations = new JSONArray();
        for (double[] coord : coordinates) {
            locations.put(toOrsPoint(coord[0], coord[1])); // coord[0]=lat, coord[1]=lon
        }

        JSONObject requestBody = new JSONObject();

        requestBody.put("locations", locations);
        requestBody.put("metrics", new JSONArray().put("distance").put("duration")); // Get both distance and duration
        requestBody.put("units", "m"); // "km" or "mi" also supported
        requestBody.put("profile", "driving-hgv");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MATRIX_URL))
            .header("Authorization", requireOrsApiKey())
                .header("Content-Type", "application/json; charset=utf-8")
                .timeout(orsRequestTimeout)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            throw new ApiNotConnectedException(
                    "ORS API request timed out after " + orsRequestTimeout.getSeconds() + " seconds.");
        }

        if (response.statusCode() != 200) 
        {
            throw new RuntimeException("ORS API error: " + response.body());
        }


        JSONObject json = new JSONObject(response.body());
        JSONArray distances = json.getJSONArray("distances");

        for (int i = 0; i < size; i++) {
            JSONArray row = distances.getJSONArray(i);
            for (int j = 0; j < size; j++) {
                if (!row.isNull(j)) {
                    matrix[i][j] = row.getLong(j);
                } else {
                    matrix[i][j] = Long.MAX_VALUE;
                }
            }
        }

        return matrix;
    }

    public long[][] getDistanceMatrixFromLocations(List<Location> locations) throws Exception{
        List<double[]> coords = locations.stream()
            .map(location -> new double[] {location.getLatitude(), location.getLongitude()})
            .collect(Collectors.toList());
        return getDistanceMatrix(coords);
    }

    public static long[][] buildHaversineDistanceMatrix(List<double[]> coordinates) {
        int size = coordinates.size();
        long[][] distanceMatrix = new long[size][size];

        for (int i = 0; i < size; i++) {
            double[] from = coordinates.get(i);
            for (int j = 0; j < size; j++) {
                double[] to = coordinates.get(j);
                if (i == j) {
                    distanceMatrix[i][j] = 0;
                } else {
                    distanceMatrix[i][j] = haversineDistance(from[0], from[1], to[0], to[1]);
                }
            }
        }
        return distanceMatrix;
    }

    private static long haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c);
    }

    private static long vincentyDistance(double lat1, double lon1, double lat2, double lon2) {
        final double a = 6378137;
        final double f = 1 / 298.257223563;
        final double b = 6356752.314245;

        double φ1 = Math.toRadians(lat1);
        double φ2 = Math.toRadians(lat2);
        double L = Math.toRadians(lon2 - lon1);
        double U1 = Math.atan((1 - f) * Math.tan(φ1));
        double U2 = Math.atan((1 - f) * Math.tan(φ2));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double λ = L, λP, iterLimit = 100;
        double sinλ, cosλ, sinσ, cosσ, σ, sinα, cosSqα, cos2σm, C;
        do {
            sinλ = Math.sin(λ);
            cosλ = Math.cos(λ);
            sinσ = Math.sqrt((cosU2 * sinλ) * (cosU2 * sinλ)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosλ)
                            * (cosU1 * sinU2 - sinU1 * cosU2 * cosλ));
            if (sinσ == 0)
                return 0;
            cosσ = sinU1 * sinU2 + cosU1 * cosU2 * cosλ;
            σ = Math.atan2(sinσ, cosσ);
            sinα = cosU1 * cosU2 * sinλ / sinσ;
            cosSqα = 1 - sinα * sinα;
            cos2σm = cosσ - 2 * sinU1 * sinU2 / cosSqα;
            if (Double.isNaN(cos2σm))
                cos2σm = 0;
            C = f / 16 * cosSqα * (4 + f * (4 - 3 * cosSqα));
            λP = λ;
            λ = L + (1 - C) * f * sinα
                    * (σ + C * sinσ * (cos2σm + C * cosσ
                            * (-1 + 2 * cos2σm * cos2σm)));
        } while (Math.abs(λ - λP) > 1e-12 && --iterLimit > 0);

        if (iterLimit == 0)
            return -1;

        double uSq = cosSqα * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384
                * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024
                * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double Δσ = B * sinσ * (cos2σm + B / 4
                * (cosσ * (-1 + 2 * cos2σm * cos2σm)
                        - B / 6 * cos2σm * (-3 + 4 * sinσ * sinσ)
                                * (-3 + 4 * cos2σm * cos2σm)));

        double s = b * A * (σ - Δσ);
        return Math.round(s);
    }

    public static void printDistanceMatrix(long[][] matrix) {
        System.out.println("Distance Matrix:");
        int size = matrix.length;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
    }

    /**
     * Low-level ORS API method that requests distance and/or duration matrices.
     * Allows flexible metric selection with boolean flags.
     * Returns an ORSMatrixResponse containing the raw API response.
     */
    private ORSMatrixResponse requestORSMatrix(
            List<double[]> sources,
            List<double[]> destinations,
            List<Location> sourceLocations,
            List<Location> destLocations,
            boolean includeDistance,
            boolean includeDuration) {
        
        // Log coordinates being sent to debug issues
        System.out.println("Sending coordinates to ORS:");
        for (int i = 0; i < sources.size(); i++) {
            System.out.printf("  Source %d: [lat=%.6f, lon=%.6f]%n", i, sources.get(i)[0], sources.get(i)[1]);
        }
        for (int i = 0; i < destinations.size(); i++) {
            System.out.printf("  Dest %d: [lat=%.6f, lon=%.6f]%n", i, destinations.get(i)[0], destinations.get(i)[1]);
        }
        
        JSONArray allLocations = new JSONArray();
        for (double[] coord : sources) {
            allLocations.put(toOrsPoint(coord[0], coord[1])); // coord[0]=lat, coord[1]=lon
        }
        int sourceOffset = allLocations.length();

        for (double[] coord : destinations) {
            allLocations.put(toOrsPoint(coord[0], coord[1])); // coord[0]=lat, coord[1]=lon
        }

        JSONArray sourceIndexes = new JSONArray();
        for (int i = 0; i < sources.size(); i++) {
            sourceIndexes.put(i);
        }

        JSONArray destinationIndexes = new JSONArray();
        for (int i = 0; i < destinations.size(); i++) {
            destinationIndexes.put(sourceOffset + i);
        }

        // Build metrics array based on flags
        JSONArray metrics = new JSONArray();
        if (includeDistance) metrics.put("distance");
        if (includeDuration) metrics.put("duration");

        JSONObject requestBody = new JSONObject();
        requestBody.put("locations", allLocations);
        requestBody.put("sources", sourceIndexes);
        requestBody.put("destinations", destinationIndexes);
        requestBody.put("metrics", metrics);
        requestBody.put("units", "m");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MATRIX_URL))
            .header("Authorization", requireOrsApiKey())
                .header("Content-Type", "application/json; charset=utf-8")
                .timeout(orsRequestTimeout)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            throw new ApiNotConnectedException(
                    "ORS API request timed out after " + orsRequestTimeout.getSeconds() + " seconds.");
        } catch (Exception e) {
            throw new ApiNotConnectedException("Failed to contact ORS API. Check that docker container is running and able to be reached.");
        }

        if (response.statusCode() != 200) {
            throw new ORSApiException("ORS API error: " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        
        // Check for null locations in response - indicates ORS doesn't have map data for those coordinates
        if (json.has("sources")) {
            JSONArray sourcesArray = json.getJSONArray("sources");
            List<LocationNotWithinORSMapException.LocationInfo> unmappedLocations = new ArrayList<>();
            
            for (int i = 0; i < sourcesArray.length(); i++) {
                if (sourcesArray.isNull(i)) {
                    if (i < sources.size() && i < sourceLocations.size()) {
                        Location loc = sourceLocations.get(i);
                        unmappedLocations.add(new LocationNotWithinORSMapException.LocationInfo(
                            i, 
                            sources.get(i)[0], 
                            sources.get(i)[1],
                            loc.getAddress()
                        ));
                    }
                }
            }
            
            if (!unmappedLocations.isEmpty()) {
                throw new LocationNotWithinORSMapException(
                    "ORS returned null for " + unmappedLocations.size() + " location(s). " +
                    "These locations are not within the ORS map data coverage.", 
                    unmappedLocations
                );
            }
        }
        
        return new ORSMatrixResponse(json, sources.size(), destinations.size());
    }

    /**
     * Convenience method to request both distance and duration in one call
     */
    private ORSMatrixResponse requestORSMatrixForBoth(
            List<double[]> sources,
            List<double[]> destinations,
            List<Location> sourceLocations,
            List<Location> destLocations) {
        return requestORSMatrix(sources, destinations, sourceLocations, destLocations, true, true);
    }

    /**
     * Convenience method to request only distance
     */
    private long[][] requestORSMatrixForDistance(
            List<double[]> sources,
            List<double[]> destinations) {
        // Create dummy locations for backward compatibility
        List<Location> dummyLocs = sources.stream()
            .map(coord -> {
                Location loc = new Location();
                loc.setLatitude(coord[0]);
                loc.setLongitude(coord[1]);
                loc.setAddress("Unknown");
                return loc;
            })
            .collect(Collectors.toList());
        ORSMatrixResponse response = requestORSMatrix(sources, destinations, dummyLocs, dummyLocs, true, false);
        return response.getDistances();
    }

public long[][] getDistanceMatrixInBatches(List<double[]> coordinates){
    int total = coordinates.size();
    long[][] fullMatrix = new long[total][total];

    System.out.println("Getting distance matrices");

    int maxBatchSize = 15;

    for (int i = 0; i < total; i += maxBatchSize) {
        int endI = Math.min(i + maxBatchSize, total);
        List<double[]> sources = coordinates.subList(i, endI);

        for (int j = 0; j < total; j += maxBatchSize) {
            int endJ = Math.min(j + maxBatchSize, total);
            List<double[]> destinations = coordinates.subList(j, endJ);

            long[][] partialMatrix = requestORSMatrixForDistance(sources, destinations);

            // Copy partial matrix into full matrix
            for (int si = 0; si < partialMatrix.length; si++) {
                for (int dj = 0; dj < partialMatrix[si].length; dj++) {
                    fullMatrix[i + si][j + dj] = partialMatrix[si][dj];
                }
            }
        }
    }

    return fullMatrix;
}


public long[][] getTravelTimeMatrix(List<double[]> coordinates) throws Exception {
    int size = coordinates.size();
    long[][] matrix = new long[size][size];

    JSONArray locations = new JSONArray();
    for (double[] coord : coordinates) {
        locations.put(toOrsPoint(coord[0], coord[1])); // coord[0]=lat, coord[1]=lon
    }

    JSONObject requestBody = new JSONObject();
    requestBody.put("locations", locations);
    requestBody.put("metrics", new JSONArray().put("duration")); // <-- duration in seconds
    requestBody.put("units", "m"); // distance units irrelevant for duration

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(MATRIX_URL))
            .header("Authorization", requireOrsApiKey())
            .header("Content-Type", "application/json; charset=utf-8")
            .timeout(orsRequestTimeout)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response;
        try {
        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
        throw new ApiNotConnectedException(
            "ORS API request timed out after " + orsRequestTimeout.getSeconds() + " seconds.");
        }

    if (response.statusCode() != 200) {
        throw new RuntimeException("ORS API error: " + response.body());
    }

    JSONObject json = new JSONObject(response.body());
    JSONArray durations = json.getJSONArray("durations");

    for (int i = 0; i < size; i++) {
        JSONArray row = durations.getJSONArray(i);
        for (int j = 0; j < size; j++) {
            if (!row.isNull(j)) {
                matrix[i][j] = row.getLong(j); // seconds
            } else {
                matrix[i][j] = Long.MAX_VALUE;
            }
        }
    }

    return matrix;
}


    /**
     * Gets both distance and travel time matrices in a single batching operation.
     * This is more efficient than calling separate methods as it combines all API requests.
     * Returns both matrices to be used in route optimization.
     */
    public DistanceAndTimeMatrices getDistanceAndTravelTimeMatricesInBatches(
            List<double[]> coordinates, 
            List<Location> locations) {
        int total = coordinates.size();
        long[][] fullDistanceMatrix = new long[total][total];
        long[][] fullTimeMatrix = new long[total][total];
        int maxBatchSize = 15;

        System.out.println("Getting distance and time matrices in combined batches");

        for (int i = 0; i < total; i += maxBatchSize) {
            int endI = Math.min(i + maxBatchSize, total);
            List<double[]> sources = coordinates.subList(i, endI);
            List<Location> sourceLocations = locations.subList(i, endI);

            for (int j = 0; j < total; j += maxBatchSize) {
                int endJ = Math.min(j + maxBatchSize, total);
                List<double[]> destinations = coordinates.subList(j, endJ);
                List<Location> destLocations = locations.subList(j, endJ);

                ORSMatrixResponse response = requestORSMatrix(sources, destinations, sourceLocations, destLocations, true, true);
                long[][] partialDistance = response.getDistances();
                long[][] partialTime = response.getDurations();

                for (int si = 0; si < partialDistance.length; si++) {
                    for (int dj = 0; dj < partialDistance[si].length; dj++) {
                        fullDistanceMatrix[i + si][j + dj] = partialDistance[si][dj];
                        fullTimeMatrix[i + si][j + dj] = partialTime[si][dj];
                    }
                }
            }
        }

        return new DistanceAndTimeMatrices(fullDistanceMatrix, fullTimeMatrix);
    }

    public long[][] getTravelTimeMatrixInBatches(List<double[]> coordinates, List<Location> locations) {
        return getDistanceAndTravelTimeMatricesInBatches(coordinates, locations).timeMatrix;
    }
    //ORS expects Long, Lat, but we store Lat, Long (I messed up creating the DB early on) so we fix it here
    private static JSONArray toOrsPoint(double lat, double lon) {
        return new JSONArray().put(lon).put(lat);
    }

    public String hashMatrix(long[][] matrix) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            StringBuilder builder = new StringBuilder();

            for (long[] row : matrix) {
                builder.append(Arrays.toString(row));
            }

            byte[] hashBytes = digest.digest(builder.toString().getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
