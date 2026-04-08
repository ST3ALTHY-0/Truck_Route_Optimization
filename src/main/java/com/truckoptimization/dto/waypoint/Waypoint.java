package com.truckoptimization.dto.waypoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Waypoint {

    private String address;
    private Integer demand;
    
}
