package com.truckoptimization.dto.direction;

import java.util.List;

import lombok.Data;

@Data
public class RouteApiResponse {

    private List<RouteResponse> routes;
    private List<Double> bbox; 
    private Object metadata; 

}
