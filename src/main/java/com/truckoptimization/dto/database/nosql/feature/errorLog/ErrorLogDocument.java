package com.truckoptimization.dto.database.nosql.feature.errorLog;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import org.springframework.data.annotation.Id;

@Data
@Document("ErrorLog")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorLogDocument {

    @Id
    private String id;

    private Level level;
    // The name of the service or component where the error occurred
    private String service;
    
    // A brief message describing the error
    private String message;

    // The type of error and any relevant details
    private Error error;

    private String stackTrace;

    
    @Builder.Default
    private Instant timestamp = Instant.now();


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Error{
        // The type of error (e.g., NullPointerException, IOException)
        private String type;
        // A detailed description of the error, including any relevant information that can help in debugging
        private String details;
    }

    public enum Level{
        INFO, WARN, ERROR
    }

}
