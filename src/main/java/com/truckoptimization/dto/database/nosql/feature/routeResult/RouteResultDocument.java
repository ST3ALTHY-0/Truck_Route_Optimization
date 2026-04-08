package com.truckoptimization.dto.database.nosql.feature.routeResult;

import java.util.List;

import com.truckoptimization.dto.results.RouteResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "route_results")
public class RouteResultDocument {

    @Id
    private String id;

    private int vehicleId;
    private List<Integer> route;
    // private int[] demand;
    private long totalDistance;
    private long totalLoad;
    private String encodedGeometry;

    public static RouteResultDocument fromDto(RouteResult dto) {
        if (dto == null) return null;

        return RouteResultDocument.builder()
                .vehicleId(dto.getVehicleId())
                .route(dto.getRoute())
                .totalDistance(dto.getTotalDistance())
                .totalLoad(dto.getTotalLoad())
                .encodedGeometry(dto.getEncodedGeometry())
                .build();
    }

    public RouteResult toDto() {
        RouteResult dto = new RouteResult(vehicleId, route, totalDistance, totalLoad);
        dto.setEncodedGeometry(encodedGeometry);
        return dto;
    }
}