package com.truckoptimization.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.truckoptimization.dto.database.nosql.feature.errorLog.ErrorLogService;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalRestExceptionHandler {

    private final ErrorLogService errorLogService;

    public GlobalRestExceptionHandler(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        errorLogService.logException(ex, "GlobalRestExceptionHandler", request);
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    @ExceptionHandler({
            InvalidInputException.class,
            DepotDemandException.class,
            DistanceMatrixException.class,
            ApiNotConnectedException.class,
            NoSolutionFoundException.class,
            ORSApiException.class,
            CoordsApiException.class,
            LocationNotWithinORSMapException.class
    })
    public ResponseEntity<ApiError> handleKnownClientExceptions(Exception ex, HttpServletRequest request) {
        errorLogService.logException(ex, "GlobalRestExceptionHandler", request);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex, HttpServletRequest request) {
        errorLogService.logException(ex, "GlobalRestExceptionHandler", request);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong: " + ex.getMessage(),
                request);
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status, String message,
            HttpServletRequest request) {
        ApiError error = new ApiError(status.value(), message, request.getRequestURI());
        return new ResponseEntity<>(error, status);
    }
}