package com.truckoptimization.task.api.geocodeMapsApi;

import java.io.IOException;
import java.time.Duration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.truckoptimization.common.util.ApiKeyUtil;
import com.truckoptimization.dto.location.Location;
import com.truckoptimization.exception.CoordsApiException;

import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// helper class for pure geocoding API access (no cache/DB awareness)

@Service
public class GeocodingService {

    private final GeocodingRateLimiter geocodingRateLimiter;
    private final String geomapsApiKey;
    private final HttpClient httpClient;

    public GeocodingService(@Value("${geomaps.api.key:}") String geomapsApiKey,
            GeocodingRateLimiter geocodingRateLimiter) {
        this.geomapsApiKey = geomapsApiKey;
        this.geocodingRateLimiter = geocodingRateLimiter;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private String requireGeomapsApiKey() {
        return ApiKeyUtil.requireApiKey(geomapsApiKey, "GEOMAPS_API_KEY");
    }

    public Location convertAddressToLatLong(String address) {
        try {
            String urlStr = "https://geocode.maps.co/search?q=" +
                    java.net.URLEncoder.encode(address, "UTF-8") +
                    "&api_key=" + requireGeomapsApiKey();

            System.out.println("Trying to get coords of: " + address);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new java.net.URI(urlStr))
                    .GET()
                    .build();

            geocodingRateLimiter.acquire();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new CoordsApiException("Geocode API error: HTTP " + response.statusCode() +
                        " while resolving address: '" + address + "'.");
            }

            JSONArray arr = new JSONArray(response.body());
            if (arr.length() > 0) {
                JSONObject obj = arr.getJSONObject(0);
                double lat = Double.parseDouble(obj.getString("lat"));
                double lon = Double.parseDouble(obj.getString("lon"));

                Location location = new Location();
                location.setAddress(address);
                location.setLatitude(lat);
                location.setLongitude(lon);
                return location;
            } else {
                // No results returned — likely a misspelling or ambiguous address
                System.out.println("No geocode results for address: '" + address + "'. It may be misspelled or too ambiguous.");
                throw new CoordsApiException("No coordinates found for address: '" + address + "'. Please check the spelling or include city/state.");
            }
        } catch (IOException ioE) {
            System.out.println("Geocode API I/O error while resolving '" + address + "': " + ioE.getMessage());
            ioE.printStackTrace();
        } catch(URISyntaxException uriE){
            System.out.println("Geocode API URI error for address '" + address + "': " + uriE.getMessage());
            uriE.printStackTrace();
        } catch(InterruptedException iE){
            Thread.currentThread().interrupt();
            System.out.println("Thread interrupted during geocode request for address '" + address + "': " + iE.getMessage());
            iE.printStackTrace();
        }
        throw new CoordsApiException("Unable to resolve coordinates for address: '" + address + "'. The location may be misspelled or unsupported.");
    }
}
