package com.truckoptimization.task.csvParser;

import org.springframework.web.multipart.MultipartFile;
import com.truckoptimization.dto.waypoint.Waypoint;

import java.io.IOException;
import java.util.List;
import com.truckoptimization.exception.InvalidInputException;

public interface CsvParser {

    List<Waypoint> parseCsv(MultipartFile file) throws InvalidInputException, IOException;
    
}
