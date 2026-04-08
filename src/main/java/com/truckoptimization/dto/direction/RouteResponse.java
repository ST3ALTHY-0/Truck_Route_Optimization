package com.truckoptimization.dto.direction;

import java.util.List;

import lombok.Data;

@Data
public class RouteResponse {

   private Summary summary;
    private List<Double> bbox;
    private Extras extras;
    private String geometry;
    private List<Integer> way_points;
    private List<Object> warnings;

}
