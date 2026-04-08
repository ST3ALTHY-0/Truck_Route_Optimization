package com.truckoptimization.dto.database.nosql.feature.errorLog;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ErrorLogService {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLogService.class);

    private final ErrorLogRepository errorLogRepository;

    public ErrorLogService(ErrorLogRepository errorLogRepository){
        this.errorLogRepository = errorLogRepository;
    }

    public ErrorLogDocument saveDocument(ErrorLogDocument document) {
        if(document == null){
            throw new IllegalArgumentException("ErrorLogDocument cannot be null");
        }
        return errorLogRepository.save(document);
    }

    public void logException(Throwable throwable, String service) {
        logException(throwable, service, null);
    }

    public void logException(Throwable throwable, String service, HttpServletRequest request) {
        if (throwable == null) {
            return;
        }

        String requestDetails = buildRequestDetails(request);
        String details = throwable.getMessage() == null ? "No exception message provided" : throwable.getMessage();
        if (!requestDetails.isBlank()) {
            details = details + " | " + requestDetails;
        }

        ErrorLogDocument document = ErrorLogDocument.builder()
                .level(ErrorLogDocument.Level.ERROR)
                .service(service)
                .message(resolveTopLevelMessage(throwable))
                .error(new ErrorLogDocument.Error(throwable.getClass().getSimpleName(), details))
                .stackTrace(extractStackTrace(throwable))
                .build();

        try {
            saveDocument(document);
        } catch (Exception loggingFailure) {
            logger.error("Failed to persist error log for service {}", service, loggingFailure);
        }
    }

    private String resolveTopLevelMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return message;
    }

    private String buildRequestDetails(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String method = request.getMethod() == null ? "UNKNOWN" : request.getMethod();
        String path = request.getRequestURI() == null ? "unknown" : request.getRequestURI();
        String query = request.getQueryString();

        if (query == null || query.isBlank()) {
            return method + " " + path;
        }

        return method + " " + path + "?" + query;
    }

    private String extractStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        }
    }
    
}
