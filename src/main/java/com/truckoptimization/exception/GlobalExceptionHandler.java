package com.truckoptimization.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.truckoptimization.dto.database.nosql.feature.errorLog.ErrorLogService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ErrorLogService errorLogService;

    public GlobalExceptionHandler(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    private String returnHomeWithError(Model model, String message) {
        model.addAttribute("errorMessage", message);
        model.addAttribute("error", message);
        return "home";
    }

    private String returnHomeWithLoggedError(Exception exception, Model model, HttpServletRequest request) {
        errorLogService.logException(exception, "GlobalExceptionHandler", request);
        model.addAttribute("formState", extractFormState(request));
        return returnHomeWithError(model, exception.getMessage());
    }

    private Map<String, Object> extractFormState(HttpServletRequest request) {
        Map<String, Object> formState = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                formState.put(key, values[0]);
            }
        });

        // HTML checkboxes are omitted when unchecked; set explicit false for deterministic hydration.
        if (!formState.containsKey("useTimeConstraints")) {
            formState.put("useTimeConstraints", false);
        }

        return formState;
    }

    @ExceptionHandler(DepotDemandException.class)
    public String handleDepotDemandException(DepotDemandException e, Model model, HttpServletRequest request) {
        return returnHomeWithLoggedError(e, model, request);
    }

    @ExceptionHandler(InvalidInputException.class)
    public String handleInvalidInputException(InvalidInputException e, Model model, HttpServletRequest request) {
        return returnHomeWithLoggedError(e, model, request);
    }

     @ExceptionHandler(DistanceMatrixException.class)
    public String handleDistanceMatrixException(DistanceMatrixException e, Model model, HttpServletRequest request) {
        return returnHomeWithLoggedError(e, model, request);
    }

    @ExceptionHandler(ApiNotConnectedException.class)
    public String handleApiNotConnectedException(ApiNotConnectedException e, Model model, HttpServletRequest request) {
        return returnHomeWithLoggedError(e, model, request);
    }

    @ExceptionHandler(NoSolutionFoundException.class)
    public String handleNoSolutionFoundException(NoSolutionFoundException e, Model model, HttpServletRequest request) {
        return returnHomeWithLoggedError(e, model, request);
    }

    @ExceptionHandler(ORSApiException.class)
    public String handleORSApiException(ORSApiException e, Model model, HttpServletRequest request) {
        return returnHomeWithLoggedError(e, model, request);
    }

    @ExceptionHandler(CoordsApiException.class)
    public String handleCoordsApiException(CoordsApiException e, Model model, HttpServletRequest request) {
        return returnHomeWithLoggedError(e, model, request);
    }

    @ExceptionHandler(LocationNotWithinORSMapException.class)
    public String handleLocationNotWithinORSMapException(LocationNotWithinORSMapException e, Model model,
            HttpServletRequest request) {
        // Keep diagnostics in application logs while also persisting structured error info.
        logger.error("ERROR: ORS Map Coverage Issue Detected");
        logger.error("The following locations are not within the ORS map data:");
        for (LocationNotWithinORSMapException.LocationInfo loc : e.getUnmappedLocations()) {
            logger.error("{}", loc.toString());
        }
        return returnHomeWithLoggedError(e, model, request);
    }

    @ExceptionHandler(Exception.class)
    public String handleAllViewExceptions(Exception e, Model model, HttpServletRequest request) {
        return returnHomeWithLoggedError(e, model, request);
    }

    

    
}
