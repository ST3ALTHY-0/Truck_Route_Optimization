package com.truckoptimization.dto.database.nosql.feature.compiledRouteResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.truckoptimization.dto.database.nosql.feature.routeResult.RouteResultDocument;
import com.truckoptimization.dto.location.Location;
import com.truckoptimization.dto.results.CompiledResults;
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
@Document(collection = "compiled_results")
public class CompiledRouteResultDocument {

    @Id
    private String id;

    private Long userId;

    @Builder.Default
    private List<RouteResultDocument> results = new ArrayList<>();
    private long[][] matrix;
    private long[][] travelTimeMatrix;
    private List<Location> locations;
    private int[] demand;

    @Builder.Default
    private Instant createdAt = Instant.now();

    public static CompiledRouteResultDocument fromDto(CompiledResults dto, Long userId) {
        if (dto == null) return null;

        List<RouteResultDocument> mappedResults = dto.getResults() == null
                ? new ArrayList<>()
                : dto.getResults().stream().map(RouteResultDocument::fromDto).toList();

        return CompiledRouteResultDocument.builder()
                .userId(userId)
                .results(mappedResults)
                .matrix(dto.getMatrix())
                .travelTimeMatrix(dto.getTravelTimeMatrix())
                .locations(dto.getLocations())
            .demand(dto.getDemand())
                .build();
    }

    public static CompiledRouteResultDocument fromDto(CompiledResults dto) {
        return fromDto(dto, null);
    }

    public CompiledResults toDto() {
        CompiledResults dto = new CompiledResults();

        List<RouteResult> mappedResults = results == null
                ? new ArrayList<>()
                : results.stream().map(RouteResultDocument::toDto).toList();

        dto.setResults(mappedResults);
        dto.setMatrix(matrix);
        dto.setTravelTimeMatrix(travelTimeMatrix);
        dto.setLocations(locations);
        dto.setDemand(demand);

        return dto;
    }
}