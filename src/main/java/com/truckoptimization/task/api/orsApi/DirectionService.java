package com.truckoptimization.task.api.orsApi;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truckoptimization.common.util.ApiKeyUtil;
import com.truckoptimization.dto.direction.RouteApiResponse;
import com.truckoptimization.dto.direction.RouteResponse;
import com.truckoptimization.dto.results.RouteResult;
import com.truckoptimization.exception.ORSApiException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

//helper class to calculate the distance matrix (how far away each location is from all other locations). We can use our 'own' algorithems to find the solution,
//which is not great because they lack precision, only accounting for straight distance, not roads; and we can use a free api to calculate this for us, limits of 500 requests per day. 

@Service
public class DirectionService {

    private static final String DISTANCE_URL = "http://localhost:8090/ors/v2/directions/driving-hgv";
    private final String orsApiKey;

    public DirectionService(@Value("${ors.api.key:}") String orsApiKey) {
        this.orsApiKey = orsApiKey;
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

    public RouteApiResponse getDirections(List<double[]> coordinates){

        try{

        JSONArray locations = new JSONArray();
        for (double[] coord : coordinates) {
            JSONArray point = new JSONArray();
            point.put(coord[1]); // longitude
            point.put(coord[0]); // latitude
            locations.put(point);
        }

        JSONObject requestBody = new JSONObject();

        requestBody.put("coordinates", locations);

        JSONObject options = new JSONObject();
        //options.put("vehicle_type", "driving-car");
        //options.put("avoid_features", new JSONArray().put("ferries"));
        //options.put("weight", 40000);
        //options.put("height", 4.0);
        // options.put("width", 2.5);

        requestBody.put("preference", "recommended");
        requestBody.put("units", "m");
        requestBody.put("profile", "driving-hgv");
        requestBody.put("geometry", true);
        requestBody.put("instructions", false);
        requestBody.put("attributes", new JSONArray().put("avgspeed").put("percentage"));
        requestBody.put("extra_info", new JSONArray().put("waytype").put("surface"));
        //requestBody.put("options", options);
        requestBody.put("maximum_speed", 90);
        requestBody.put("geometry_simplify", false);
        requestBody.put("language", "en");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DISTANCE_URL))
                .header("Authorization", requireOrsApiKey())
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("ORS API error: " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        // System.out.println("[DEBUG] ORS Response route0s array: " + json.optJSONArray("routes"));
        if (json.has("routes") && json.getJSONArray("routes").length() > 0) {
            JSONObject firstRoute = json.getJSONArray("routes").getJSONObject(0);
            // System.out.println("[DEBUG] First route geometry: " + firstRoute.opt("geometry"));
        }

        ObjectMapper mapper = new ObjectMapper();
        RouteApiResponse routes = mapper.readValue(response.body(), RouteApiResponse.class);

        return routes;
    }catch(Exception e){
        e.printStackTrace();
    }
    throw new ORSApiException("Could not get directions to locations. Check ORS API");
    }

    public RouteResult addGeometryToRouteResult(RouteResponse routeResponse, RouteResult routeResult) {
        if (routeResponse == null || routeResponse == null) {
            throw new IllegalArgumentException("RouteResponse does not contain any routes.");
        }
        if (routeResponse.getGeometry() == null) {
            throw new IllegalArgumentException("Route does not contain 'geometry' field.");
        }
        routeResult.setEncodedGeometry(routeResponse.getGeometry());
        return routeResult;
    }

}
