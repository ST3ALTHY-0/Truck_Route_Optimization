package com.truckoptimization.task.optimizeRoute;

import java.util.List;

import com.truckoptimization.common.config.RoutingConfig;
import com.truckoptimization.dto.results.RouteResult;


public interface RouteOptimizer {

        List<RouteResult> optimizeRoutes(long[][] distanceMatrixCords, int[] demands, RoutingConfig routingConfig);
} 
