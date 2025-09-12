package ar.edu.utn.dds.k3003.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.security.InvalidParameterException;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final MeterRegistry meter;

    @Autowired
    public GlobalExceptionHandler(MeterRegistry meter) { this.meter = meter; }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElementException(NoSuchElementException e) {
        meter.counter("fuentes.http.errores", "exception","NoSuchElementException").increment();

        Map<String, String> response = new HashMap<>();
        response.put("error", "Not Found");
        response.put("message", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParameterException(InvalidParameterException e) {
        meter.counter("fuentes.http.errores", "exception","InvalidParameterException").increment();

        Map<String, String> response = new HashMap<>();
        response.put("error", "Bad Request");
        response.put("message", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        meter.counter("fuentes.http.errores", "exception","Exception").increment();

        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 