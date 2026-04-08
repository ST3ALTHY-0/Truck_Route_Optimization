package com.truckoptimization.integration.geocodeMaps;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AddressToLongLatApiTest {

	private static final String BASE_URL = "https://geocode.maps.co/search";

	@Test
	@DisplayName("Geocode API should return latitude/longitude for a known address")
	void shouldResolveAddressToCoordinates() throws Exception {
		String apiKey = resolveApiKey();
		Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(),
				"Skipping integration test: set GEOCODE_MAPS_API_KEY or GEOCODE_API_KEY");

		String address = "Indianapolis, IN";
		String url = BASE_URL + "?q=" + URLEncoder.encode(address, StandardCharsets.UTF_8)
				+ "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.GET()
				.build();

		HttpResponse<String> response = HttpClient.newHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		assertTrue(response.statusCode() == 200,
				"Expected HTTP 200 from geocode API, got " + response.statusCode());

		JSONArray results = new JSONArray(response.body());
		assertFalse(results.isEmpty(), "Expected at least one geocode result");

		JSONObject first = results.getJSONObject(0);
		assertNotNull(first.getString("lat"), "lat should be present");
		assertNotNull(first.getString("lon"), "lon should be present");

		double lat = Double.parseDouble(first.getString("lat"));
		double lon = Double.parseDouble(first.getString("lon"));

		assertTrue(lat >= -90 && lat <= 90, "Latitude should be within [-90, 90]");
		assertTrue(lon >= -180 && lon <= 180, "Longitude should be within [-180, 180]");
	}

	private static String resolveApiKey() {
		String value = System.getenv("GEOCODE_MAPS_API_KEY");
		if (value != null && !value.isBlank()) {
			return value;
		}
		value = System.getenv("GEOCODE_API_KEY");
		if (value != null && !value.isBlank()) {
			return value;
		}
		value = System.getProperty("GEOCODE_MAPS_API_KEY");
		if (value != null && !value.isBlank()) {
			return value;
		}
		return null;
	}
}
