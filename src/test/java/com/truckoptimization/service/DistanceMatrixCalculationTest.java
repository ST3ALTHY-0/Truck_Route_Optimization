package com.truckoptimization.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.truckoptimization.dto.location.Location;
import com.truckoptimization.task.api.orsApi.DistanceMatrixCalculation;

@DisplayName("DistanceMatrixCalculation Unit Tests")
class DistanceMatrixCalculationTest {

    private List<double[]> testCoordinates;
    private List<Location> testLocations;

    private static final List<double[]> MIDWEST_COORDINATES = List.of(
        new double[]{41.8781, -87.6298}, // Chicago, IL
        new double[]{39.7684, -86.1581}, // Indianapolis, IN
        new double[]{41.4993, -81.6944}, // Cleveland, OH
        new double[]{42.3314, -83.0458}, // Detroit, MI
        new double[]{43.0389, -87.9065}, // Milwaukee, WI
        new double[]{44.9778, -93.2650}, // Minneapolis, MN
        new double[]{39.0997, -94.5786}, // Kansas City, MO
        new double[]{41.5868, -93.6250}, // Des Moines, IA
        new double[]{43.0731, -89.4012}, // Madison, WI
        new double[]{41.2565, -95.9345}, // Omaha, NE
        new double[]{39.1031, -84.5120}, // Cincinnati, OH
        new double[]{39.9612, -82.9988}, // Columbus, OH
        new double[]{41.0814, -81.5190}, // Akron, OH
        new double[]{41.0793, -85.1394}, // Fort Wayne, IN
        new double[]{40.8136, -96.7026}  // Lincoln, NE
    );

    @BeforeEach
    void setUp() {
        testCoordinates = Arrays.asList(
                new double[]{39.7684, -86.1581}, // Indianapolis
                new double[]{41.4925, -81.6944}, // Cleveland
                new double[]{41.8781, -87.6298}  // Chicago
        );

        testLocations = Arrays.asList(
                createLocation("Indianapolis, IN", 39.7684, -86.1581),
                createLocation("Cleveland, OH", 41.4925, -81.6944),
                createLocation("Chicago, IL", 41.8781, -87.6298)
        );
    }

    // ====================
    // Haversine Distance Tests
    // ====================

    @Test
    @DisplayName("Should calculate Haversine distance matrix correctly")
    void testBuildHaversineDistanceMatrix() {
        long[][] matrix = DistanceMatrixCalculation.buildHaversineDistanceMatrix(testCoordinates);

        assertNotNull(matrix);
        assertEquals(3, matrix.length);
        assertEquals(3, matrix[0].length);

        // Diagonal = 0
        assertEquals(0, matrix[0][0]);
        assertEquals(0, matrix[1][1]);
        assertEquals(0, matrix[2][2]);

        // Symmetry
        assertEquals(matrix[0][1], matrix[1][0]);
        assertEquals(matrix[0][2], matrix[2][0]);
        assertEquals(matrix[1][2], matrix[2][1]);

        // Positive distances
        assertTrue(matrix[0][1] > 0);
        assertTrue(matrix[0][2] > 0);
        assertTrue(matrix[1][2] > 0);

        // Approx distance Indianapolis -> Cleveland ~500km
        assertTrue(matrix[0][1] > 400000 && matrix[0][1] < 600000);
    }

    @Test
    @DisplayName("Should handle single location")
    void testHaversineWithSingleLocation() {
        List<double[]> single = Arrays.asList(new double[]{39.7684, -86.1581});
        long[][] matrix = DistanceMatrixCalculation.buildHaversineDistanceMatrix(single);

        assertEquals(1, matrix.length);
        assertEquals(1, matrix[0].length);
        assertEquals(0, matrix[0][0]);
    }

    @Test
    @DisplayName("Should handle two locations")
    void testHaversineWithTwoLocations() {
        List<double[]> twoLocations = Arrays.asList(
                new double[]{39.7684, -86.1581},
                new double[]{41.4925, -81.6944}
        );
        long[][] matrix = DistanceMatrixCalculation.buildHaversineDistanceMatrix(twoLocations);

        assertEquals(2, matrix.length);
        assertEquals(matrix[0][1], matrix[1][0]);
    }

    @Test
    @DisplayName("Should handle edge coordinates")
    void testHaversineWithEdgeCaseCoordinates() {
        List<double[]> equator = Arrays.asList(
                new double[]{0.0, 0.0},
                new double[]{0.0, 1.0}
        );
        long[][] matrix = DistanceMatrixCalculation.buildHaversineDistanceMatrix(equator);

        assertNotNull(matrix);
        assertTrue(matrix[0][1] > 0);
        assertEquals(matrix[0][1], matrix[1][0]);
    }

    @Test
    @DisplayName("Should maintain symmetry in distance matrix")
    void testMatrixSymmetry() {
        long[][] matrix = DistanceMatrixCalculation.buildHaversineDistanceMatrix(testCoordinates);

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                assertEquals(matrix[i][j], matrix[j][i]);
            }
        }
    }

    @Test
    @DisplayName("Should maintain triangle inequality in distance matrix")
    void testTriangleInequality() {
        long[][] matrix = DistanceMatrixCalculation.buildHaversineDistanceMatrix(testCoordinates);

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                for (int k = 0; k < matrix.length; k++) {
                    assertTrue(matrix[i][j] <= matrix[i][k] + matrix[k][j]);
                }
            }
        }
    }

    // ====================
    // Batching / Coordinate Helpers
    // ====================

    @Test
    @DisplayName("Should create correct coordinate list for batching")
    void testCoordinateListCreation() {
        List<double[]> list = createCoordinateList(20);
        assertEquals(20, list.size());

        // Should loop over MIDWEST_COORDINATES
        for (int i = 0; i < 20; i++) {
            assertArrayEquals(MIDWEST_COORDINATES.get(i % MIDWEST_COORDINATES.size()), list.get(i));
        }
    }

    @Test
    @DisplayName("Should convert Location to coordinates correctly")
    void testLocationToCoordinatesConversion() {
        List<double[]> coords = testLocations.stream()
                .map(loc -> new double[]{loc.getLatitude(), loc.getLongitude()})
                .toList();

        assertEquals(3, coords.size());
        assertEquals(39.7684, coords.get(0)[0]);
        assertEquals(-86.1581, coords.get(0)[1]);
    }

    // ====================
    // Helpers
    // ====================

    private Location createLocation(String address, double latitude, double longitude) {
        Location loc = new Location();
        loc.setAddress(address);
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        return loc;
    }

    private List<double[]> createCoordinateList(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> MIDWEST_COORDINATES.get(i % MIDWEST_COORDINATES.size()))
                .toList();
    }
}
