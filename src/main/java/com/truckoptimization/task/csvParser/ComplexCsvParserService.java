package com.truckoptimization.task.csvParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.truckoptimization.dto.csvRecord.DeliveryRecord;
import com.truckoptimization.dto.location.Location;

@Service
public class ComplexCsvParserService {

    public List<DeliveryRecord> parse(MultipartFile file) throws IOException {
        List<DeliveryRecord> records = new ArrayList<>();

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = CSVFormat.DEFAULT
                        .builder()
                        .setIgnoreEmptyLines(false)
                        .setTrim(true)
                        .setSkipHeaderRecord(false) // assuming headers are manually skipped
                        .get()
                        .parse(reader)) {
            List<CSVRecord> csvRecords = csvParser.getRecords();

            //once we find a row with less than 3 columns we return our records
            if (csvRecords.size() < 2)
                return records;

            CSVRecord dateHeaderRecord = csvRecords.get(0); // first header line
            CSVRecord headerRecord = csvRecords.get(1); // second header line
            int liftgateIndex = -1;
            int cityIndex = -1;
            int stateIndex = -1;

            List<LocalDate> dates = new ArrayList<>();
            for (String h : dateHeaderRecord) {
                if (h.matches("\\d{1,2}/\\d{1,2}/\\d{4}.*")) {
                    LocalDate date = LocalDate.parse(h.split(" ")[0], DateTimeFormatter.ofPattern("M/d/yyyy"));
                    dates.add(date);
                }
            }

            // System.out.println("Parsed dates: " + dates.toString());

            for (int i = 0; i < headerRecord.size(); i++) {
                String header = headerRecord.get(i).trim();
                if ("Liftgate".equalsIgnoreCase(header))
                    liftgateIndex = i;
                if ("City".equalsIgnoreCase(header))
                    cityIndex = i;
                if ("State".equalsIgnoreCase(header))
                    stateIndex = i;
            }

            // System.out.println("Liftgate index: " + liftgateIndex);
            // System.out.println("City index: " + cityIndex);
            // System.out.println("State index: " + stateIndex);

            for (int i = 2; i < csvRecords.size(); i++) { // data starts from line 3
                CSVRecord record = csvRecords.get(i);

                //make sure that we skip any lines that don't have enough columns to contain a city and state
                if (record.size() <= Math.max(cityIndex, stateIndex))
                    continue;

                // System.out.println("Parsing line: " + record);
                String city = (cityIndex >= 0 && cityIndex < record.size()) ? record.get(cityIndex).trim() : "";
                String state = (stateIndex >= 0 && stateIndex < record.size()) ? record.get(stateIndex).trim() : "";
                if (city.isEmpty() && state.isEmpty())
                    continue;

                String address = (city + ", " + state).toUpperCase();
                if (address == null || address.trim().equals(","))
                    continue;

                //System.out.println("Address: " + address);

                int offset = liftgateIndex + 1; // starting point for the first Plts of the first date


                //we take the amount of dates we have in the csv and create a record for all the different dates we need to deliver to a certain address
                for (int d = 0; d < dates.size(); d++) {
                    int baseIndex = offset + d * 3;

                    if (baseIndex + 2 >= record.size())
                        break;

                    String palletsStr = record.get(baseIndex).trim();
                    String piecesStr = record.get(baseIndex + 1).trim();
                    String weightStr = record.get(baseIndex + 2).trim();

                    if (isInteger(palletsStr) && isInteger(piecesStr) && isInteger(weightStr)) {
                        int pallets = Integer.parseInt(palletsStr);
                        int pieces = Integer.parseInt(piecesStr);
                        int weight = Integer.parseInt(weightStr);

                        //one of these values must be above 0
                        if (pallets > 0 || pieces > 0 || weight > 0) {
                            
                            DeliveryRecord r = new DeliveryRecord();
                            // Create and set Location object, then set address
                            Location location = new Location();
                            location.setAddress(address);
                            r.setLocation(location);
                            r.setDate(dates.get(d)); 
                            r.setPallets(pallets);
                            r.setPieces(pieces);
                            r.setWeight(weight);
                            r.setDeliveries(1);
                            records.add(r);
                        }
                    }
                }
            }
        }

        return records;
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
