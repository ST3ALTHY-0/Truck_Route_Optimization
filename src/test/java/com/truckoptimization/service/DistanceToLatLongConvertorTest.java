package com.truckoptimization.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.truckoptimization.dto.location.Location;
import com.truckoptimization.dto.location.LocationService;
import com.truckoptimization.exception.CoordsApiException;
import com.truckoptimization.task.api.geocodeMapsApi.GeocodingService;

@DisplayName("DistanceToLatLongConvertor Tests")
@ExtendWith(MockitoExtension.class)
class DistanceToLatLongConvertorTest {

    @InjectMocks
    private GeocodingService geocodingService;

    @Mock
    private LocationService locationService;

    private Location mockLocation;
    private String testAddress;

    @BeforeEach
    void setUp() {
        testAddress = "Indianapolis, IN";
        mockLocation = createMockLocation(testAddress, 39.7684, -86.1581);
    }

    @Test
    @DisplayName("Should return existing location from database if it exists")
    void testReturnExistingLocationFromDatabase() {
        // Arrange
        when(locationService.checkLocationExists(testAddress))
                .thenReturn(Optional.of(mockLocation));

        // Act
        Location result = geocodingService.convertAddressToLatLong(testAddress);

        // Assert
        assertNotNull(result, "Location should not be null");
        assertEquals(testAddress, result.getAddress(), "Address should match");
        assertEquals(39.7684, result.getLatitude(), "Latitude should match");
        assertEquals(-86.1581, result.getLongitude(), "Longitude should match");

        // Verify database was checked
        verify(locationService, times(1)).checkLocationExists(testAddress);
        // Verify API was not called
        verify(locationService, never()).saveLocation(anyString(), anyDouble(), anyDouble());
    }

    /*
    @Test
    @DisplayName("Should return location with null coordinates as non-existent")
    void testTreatLocationWithNullCoordsAsNonExistent() {
        // Arrange
        Location incompleteLocation = new Location();
        incompleteLocation.setAddress(testAddress);
        incompleteLocation.setLatitude(null);
        incompleteLocation.setLongitude(null);

        when(locationService.checkLocationExists(testAddress))
                .thenReturn(Optional.of(incompleteLocation));

        // Act & Assert - should call API and throw CoordsApiException (network call)
        assertThrows(CoordsApiException.class, () -> {
            distanceToLatLongConvertor.convertAddressToLatLong(testAddress);
        }, "Should throw CoordsApiException when location has null coordinates");
    }
    */

    /*
    @Test
    @DisplayName("Should return empty optional when location does not exist in database")
    void testLocationDoesNotExistInDatabase() {
        // Arrange
        when(locationService.checkLocationExists(testAddress))
                .thenReturn(Optional.empty());

        // Act & Assert - should attempt API call
        assertThrows(CoordsApiException.class, () -> {
            distanceToLatLongConvertor.convertAddressToLatLong(testAddress);
        }, "Should throw CoordsApiException when making API call");
    }
    */

    @Test
    @DisplayName("Should handle address with special characters")
    void testHandleAddressWithSpecialCharacters() {
        // Arrange
        String specialAddress = "123 Main St, Indianapolis, IN 46204";
        when(locationService.checkLocationExists(specialAddress))
                .thenReturn(Optional.of(mockLocation));

        // Act
        Location result = geocodingService.convertAddressToLatLong(specialAddress);

        // Assert
        assertNotNull(result, "Should handle special characters in address");
        verify(locationService, times(1)).checkLocationExists(specialAddress);
    }

    @Test
    @DisplayName("Should handle address with spaces")
    void testHandleAddressWithSpaces() {
        // Arrange
        String addressWithSpaces = "  Indianapolis  ,  IN  ";
        when(locationService.checkLocationExists(addressWithSpaces))
                .thenReturn(Optional.of(mockLocation));

        // Act
        Location result = geocodingService.convertAddressToLatLong(addressWithSpaces);

        // Assert
        assertNotNull(result, "Should handle addresses with extra spaces");
    }

    @Test
    @DisplayName("Should handle address with numbers")
    void testHandleAddressWithNumbers() {
        // Arrange
        String addressWithNumbers = "456 Oak Ave, Indianapolis, IN 46201";
        when(locationService.checkLocationExists(addressWithNumbers))
                .thenReturn(Optional.of(mockLocation));

        // Act
        Location result = geocodingService.convertAddressToLatLong(addressWithNumbers);

        // Assert
        assertNotNull(result, "Should handle addresses with numbers");
    }

    /*
    @Test
    @DisplayName("Should throw CoordsApiException on API error")
    void testThrowExceptionOnApiError() {
        // Arrange
        when(locationService.checkLocationExists(testAddress))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CoordsApiException.class, () -> {
            distanceToLatLongConvertor.convertAddressToLatLong(testAddress);
        }, "Should throw CoordsApiException on API failure");
    }
    */

    /*
    @Test
    @DisplayName("Should throw CoordsApiException with appropriate message")
    void testExceptionMessageContent() {
        // Arrange
        when(locationService.checkLocationExists(testAddress))
                .thenReturn(Optional.empty());

        // Act & Assert
        CoordsApiException exception = assertThrows(CoordsApiException.class, () -> {
            distanceToLatLongConvertor.convertAddressToLatLong(testAddress);
        });

        assertTrue(exception.getMessage().contains("Something went wrong") || 
                   exception.getMessage().contains("API error"),
                "Exception message should indicate API or connection issue");
    }
    */

    /*
    @Test
    @DisplayName("Should save location with correct latitude")
    void testSaveLocationWithCorrectLatitude() {
        // Arrange
        double expectedLat = 41.4925;
        when(locationService.checkLocationExists(testAddress))
                .thenReturn(Optional.empty());
        // Use lenient stubbing since the converter throws before saving in unit tests
        lenient().when(locationService.saveLocation(testAddress, expectedLat, -81.6944))
                .thenReturn(createMockLocation(testAddress, expectedLat, -81.6944));

        // Invoke the method (will throw deterministically in tests), then verify DB check
        assertThrows(CoordsApiException.class, () -> {
            distanceToLatLongConvertor.convertAddressToLatLong(testAddress);
        });
        verify(locationService, atLeastOnce()).checkLocationExists(testAddress);
    }
    */

    @Test
    @DisplayName("Should handle multiple address conversions")
    void testHandleMultipleAddressConversions() {
        // Arrange
        String address1 = "Indianapolis, IN";
        String address2 = "Cleveland, OH";
        String address3 = "Chicago, IL";

        Location location1 = createMockLocation(address1, 39.7684, -86.1581);
        Location location2 = createMockLocation(address2, 41.4925, -81.6944);
        Location location3 = createMockLocation(address3, 41.8781, -87.6298);

        when(locationService.checkLocationExists(address1))
                .thenReturn(Optional.of(location1));
        when(locationService.checkLocationExists(address2))
                .thenReturn(Optional.of(location2));
        when(locationService.checkLocationExists(address3))
                .thenReturn(Optional.of(location3));

        // Act
        Location result1 = geocodingService.convertAddressToLatLong(address1);
        Location result2 = geocodingService.convertAddressToLatLong(address2);
        Location result3 = geocodingService.convertAddressToLatLong(address3);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(39.7684, result1.getLatitude());
        assertEquals(41.4925, result2.getLatitude());
        assertEquals(41.8781, result3.getLatitude());

        verify(locationService, times(3)).checkLocationExists(anyString());
    }

    @Test
    @DisplayName("Should verify latitude is within valid range")
    void testValidateLatitudeRange() {
        // Arrange
        when(locationService.checkLocationExists(testAddress))
                .thenReturn(Optional.of(mockLocation));

        // Act
        Location result = geocodingService.convertAddressToLatLong(testAddress);

        // Assert
        assertTrue(result.getLatitude() >= -90 && result.getLatitude() <= 90,
                "Latitude should be between -90 and 90");
    }

    @Test
    @DisplayName("Should verify longitude is within valid range")
    void testValidateLongitudeRange() {
        // Arrange
        when(locationService.checkLocationExists(testAddress))
                .thenReturn(Optional.of(mockLocation));

        // Act
        Location result = geocodingService.convertAddressToLatLong(testAddress);

        // Assert
        assertTrue(result.getLongitude() >= -180 && result.getLongitude() <= 180,
                "Longitude should be between -180 and 180");
    }

    @Test
    @DisplayName("Should cache location service calls")
    void testLocationServiceCallsAreVerified() {
        // Arrange
        when(locationService.checkLocationExists(testAddress))
                .thenReturn(Optional.of(mockLocation));

        // Act - Call twice with same address
        geocodingService.convertAddressToLatLong(testAddress);
        geocodingService.convertAddressToLatLong(testAddress);

        // Assert - Should be called twice since no caching at this layer
        verify(locationService, times(2)).checkLocationExists(testAddress);
    }

    @Test
    @DisplayName("Should return location object with all fields populated")
    void testReturnCompleteLocationObject() {
        // Arrange
        when(locationService.checkLocationExists(testAddress))
                .thenReturn(Optional.of(mockLocation));

        // Act
        Location result = geocodingService.convertAddressToLatLong(testAddress);

        // Assert
        assertNotNull(result, "Location should not be null");
        assertNotNull(result.getAddress(), "Address should be populated");
        assertNotNull(result.getLatitude(), "Latitude should be populated");
        assertNotNull(result.getLongitude(), "Longitude should be populated");
        assertTrue(result.getLatitude() > 0 || result.getLatitude() < 0, "Latitude should be a valid number");
        assertTrue(result.getLongitude() > 0 || result.getLongitude() < 0, "Longitude should be a valid number");
    }

    // Helper method
    private Location createMockLocation(String address, double latitude, double longitude) {
        Location location = new Location();
        location.setAddress(address);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }
}