package com.truckoptimization.dto.database.nosql.feature.matrixCache;

import org.springframework.data.mongodb.core.mapping.Document;

import com.truckoptimization.dto.waypoint.Waypoint;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
 Document to cache distance and time matrices for quick lookup to save time on repeat requests

*/

@Document(collection = "matrix_cache")
@Data
@NoArgsConstructor
public class MatrixDocument{
    @Id
    private String id;
    // we are going to store the hash of the distance matrix for quick lookup
    private List<Waypoint> waypoints;
    //We use the built in hashCode RN, should consider using stronger SHA 256 (vs 32 bit Integer) to reduce chance of collisions
    private Integer waypointsHash;
    private long[][] distanceMatrix;
    private long[][] travelTimeMatrix;

    public void setWaypoints(List<Waypoint> waypoints){
        this.waypoints = waypoints;
        this.waypointsHash = waypoints.hashCode();;
}

}
