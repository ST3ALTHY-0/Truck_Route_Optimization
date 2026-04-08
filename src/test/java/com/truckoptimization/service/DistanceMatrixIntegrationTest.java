package com.truckoptimization.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.truckoptimization.dto.location.Location;
import com.truckoptimization.task.api.orsApi.DistanceMatrixCalculation;

@DisplayName("DistanceMatrixCalculation Integration Tests (Docker/ORS)")
class DistanceMatrixIntegrationTest {

    @Autowired
    private DistanceMatrixCalculation distanceMatrixCalculation;

    private static final long INVALID_DISTANCE = Long.MAX_VALUE;
    private static final double SYMMETRY_TOLERANCE_PERCENT = 0.01; // Allow 0.01% variance

    private List<double[]> testCoordinates;
    private List<Location> testLocations;

    @BeforeEach
    void setUp() {

        testCoordinates = Arrays.asList(
                new double[] { 39.7684, -86.1581 }, // Indianapolis, IN
                new double[] { 41.4925, -81.6944 }, // Cleveland, OH
                new double[] { 41.8781, -87.6298 } // Chicago, IL
        );

        testLocations = Arrays.asList(
                createLocation("Indianapolis, IN", 39.7684, -86.1581),
                createLocation("Cleveland, OH", 41.4925, -81.6944),
                createLocation("Chicago, IL", 41.8781, -87.6298));
    }

    @Test
    @DisplayName("Should get distance matrix from coordinates (Docker/ORS)")
    void testGetDistanceMatrixFromCoordinates() throws Exception {
        long[][] matrix = distanceMatrixCalculation.getDistanceMatrixInBatches(testCoordinates);

        assertNotNull(matrix, "Distance matrix should not be null");
        assertEquals(3, matrix.length, "Matrix should have 3 rows");
        assertEquals(3, matrix[0].length, "Matrix should have 3 columns");

        // Off-diagonal distances > 0 (skip diagonal - ORS may return MAX_VALUE)
        int validCount = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i != j && matrix[i][j] != INVALID_DISTANCE) {
                    assertTrue(matrix[i][j] > 0,
                            "Distance should be positive for different points at [" + i + "][" + j + "]");
                    validCount++;
                }
            }
        }

        assertTrue(validCount > 0, "At least some valid distances should exist");
    }

    // @Test
    // @DisplayName("Should get travel time matrix from coordinates (Docker/ORS)")
    // void testGetTravelTimeMatrixFromCoordinates() throws Exception {
    //     long[][] travelMatrix = distanceMatrixCalculation.getTravelTimeMatrixInBatches(testCoordinates);

    //     assertNotNull(travelMatrix, "Travel time matrix should not be null");
    //     assertEquals(3, travelMatrix.length, "Matrix should have 3 rows");
    //     assertEquals(3, travelMatrix[0].length, "Matrix should have 3 columns");

    //     // Off-diagonal travel times > 0 (skip diagonal and INVALID)
    //     int validCount = 0;
    //     for (int i = 0; i < 3; i++) {
    //         for (int j = 0; j < 3; j++) {
    //             if (i != j && travelMatrix[i][j] != INVALID_DISTANCE) {
    //                 assertTrue(travelMatrix[i][j] > 0,
    //                         "Travel time should be positive for different points at [" + i + "][" + j + "]");
    //                 validCount++;
    //             }
    //         }
    //     }

    //     assertTrue(validCount > 0, "At least some valid travel times should exist");
    // }

    @Test
    @DisplayName("Should convert Location objects to distance matrix")
    void testGetDistanceMatrixFromLocations() throws Exception {
        long[][] matrix = distanceMatrixCalculation.getDistanceMatrixFromLocations(testLocations);

        assertNotNull(matrix, "Distance matrix should not be null");
        assertEquals(testLocations.size(), matrix.length, "Matrix rows should match location count");
        assertEquals(testLocations.size(), matrix[0].length, "Matrix columns should match location count");

        // Verify at least some off-diagonal elements are valid
        int validCount = 0;
        for (int i = 0; i < testLocations.size(); i++) {
            for (int j = 0; j < testLocations.size(); j++) {
                if (i != j && matrix[i][j] != INVALID_DISTANCE) {
                    validCount++;
                }
            }
        }

        assertTrue(validCount > 0, "At least some valid distances should exist in matrix");
    }

    @Test
    @DisplayName("Should maintain approximate symmetry in distance matrix")
    void testDistanceMatrixSymmetry() throws Exception {
        long[][] matrix = distanceMatrixCalculation.getDistanceMatrixInBatches(testCoordinates);

        // Check symmetry with tolerance for rounding differences
        int validSymmetryChecks = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (i != j) {
                    long dist_ij = matrix[i][j];
                    long dist_ji = matrix[j][i];

                    // Skip if either is invalid
                    if (dist_ij == INVALID_DISTANCE || dist_ji == INVALID_DISTANCE) {
                        continue;
                    }

                    // Allow small percentage variance due to rounding
                    double variance = Math.abs(dist_ij - dist_ji) / (double) Math.max(dist_ij, dist_ji);
                    assertTrue(variance <= SYMMETRY_TOLERANCE_PERCENT,
                            "Distance [" + i + "][" + j + "]=" + dist_ij +
                                    " should approximately equal [" + j + "][" + i + "]=" + dist_ji +
                                    " (variance: " + (variance * 100) + "%)");
                    validSymmetryChecks++;
                }
            }
        }

        assertTrue(validSymmetryChecks > 0, "Should have at least some valid distances to check symmetry");
    }

@Test
@DisplayName("Should return positive distances between cities")
void testPositiveDistancesBetweenCities() throws Exception {
    long[][] matrix = distanceMatrixCalculation.getDistanceMatrixInBatches(testCoordinates);

    // Indianapolis [0] to Cleveland [1]
    if (matrix[0][1] != INVALID_DISTANCE) {
        assertTrue(matrix[0][1] > 400000 && matrix[0][1] < 600000,
                "Indianapolis to Cleveland should be ~475-550km in meters, got: " + matrix[0][1]);
    }

    // Indianapolis [0] to Chicago [2]
    if (matrix[0][2] != INVALID_DISTANCE) {
        assertTrue(matrix[0][2] > 250000 && matrix[0][2] < 450000,
                "Indianapolis to Chicago should be ~250-450km in meters, got: " + matrix[0][2]);
    }

    // Cleveland [1] to Chicago [2]
    if (matrix[1][2] != INVALID_DISTANCE) {
        assertTrue(matrix[1][2] > 600000 && matrix[1][2] < 1000000,
                "Cleveland to Chicago should be ~600-1000km in meters, got: " + matrix[1][2]);
    }
}

    // Helper
    private Location createLocation(String address, double latitude, double longitude) {
        Location loc = new Location();
        loc.setAddress(address);
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        return loc;
    }
}