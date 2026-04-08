package com.truckoptimization.task.csvParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.truckoptimization.dto.waypoint.Waypoint;
import com.truckoptimization.exception.InvalidInputException;
import com.truckoptimization.task.controller.support.OptimizationFormSupportService;

@Service
public class SimpleCsvParserService implements CsvParser {

    private final OptimizationFormSupportService optimizationFormSupportService;

    public SimpleCsvParserService(OptimizationFormSupportService optimizationFormSupportService) {
        this.optimizationFormSupportService = optimizationFormSupportService;
    }

    @Override
    public List<Waypoint> parseCsv(MultipartFile file) throws InvalidInputException, IOException {
        if (file == null || file.isEmpty()) {
            throw new InvalidInputException("CSV file is empty.");
        }

        List<Waypoint> parsedRows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = CSVFormat.DEFAULT
                        .builder()
                        .setIgnoreEmptyLines(true)
                        .setTrim(true)
                        .setSkipHeaderRecord(false)
                        .get()
                        .parse(reader)) {

            for (CSVRecord record : csvParser) {
                Waypoint waypoint = parseRecord(record);

                if (waypoint == null) {
                    continue;
                }

                parsedRows.add(waypoint);
            }
        }

        if (parsedRows.isEmpty()) {
            throw new InvalidInputException("No valid address/demand rows were found in the CSV.");
        }

        return parsedRows;
    }

    private Waypoint parseRecord(CSVRecord record) {
        if (record == null || record.size() == 0) {
            return null;
        }

        String address;
        String demandText;

        if (record.size() >= 2) {
            address = record.get(0).trim();
            demandText = record.get(1).trim();
        } else {
            String[] parts = record.get(0).split(":", 2);
            if (parts.length < 2) {
                return null;
            }
            address = parts[0].trim();
            demandText = parts[1].trim();
        }

        if (address.isBlank() || !isNumeric(demandText)) {
            return null;
        }

        return new Waypoint(address, Integer.parseInt(demandText));
    }

    private boolean isNumeric(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
