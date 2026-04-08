package com.truckoptimization.dto.csvRecord;

import java.time.LocalDate;

import com.truckoptimization.dto.location.Location;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

//delivery record for my own csv parser, has a very specific format and not going to be used much

@Data
// @Entity //remove annotation for now, if we want to save this data later we can add it back
public class DeliveryRecord {

    private static long idCounter = 0;
    
    @Id
    private long id;

    public DeliveryRecord() {
        this.id = ++idCounter;
    }
     private Location location;
    private LocalDate date;
    private int deliveries;
    private int weight;
    private int pallets;
    private int pieces;

}
