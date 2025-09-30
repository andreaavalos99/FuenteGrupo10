package ar.edu.utn.dds.k3003.config;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.security.InvalidParameterException;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final MeterRegistry meter;

    @Autowired
    public GlobalExceptionHandler(MeterRegistry meter) { this.meter = meter; }

    private ResponseEntity<Map<String,String>> build(HttpStatus status, String error, String message,
                                                     HttpServletRequest req, String exceptionName) {
        meter.counter("fuentes.http.errores", "exception", exceptionName).increment();

        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("message", (message == null || message.isBlank()) ? error : message);
        body.put("path", req.getRequestURI());
        body.put("requestId", Optional.ofNullable(req.getHeader("X-Request-Id"))
                .orElse(UUID.randomUUID().toString()));
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElementException(NoSuchElementException e,
                                                                            HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", e.getMessage(), req, "NoSuchElementException");
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParameterException(InvalidParameterException e,
                                                                               HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage(), req, "InvalidParameterException");
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String,String>> handleBadRequest(Exception e, HttpServletRequest req) {
        String msg =
                (e instanceof HttpMessageNotReadableException) ? "Body inválido o malformado" :
                        (e instanceof MissingServletRequestParameterException) ? "Falta un parámetro requerido" :
                                (e instanceof MethodArgumentTypeMismatchException) ? "Tipo de parámetro inválido" :
                                        e.getMessage();
        return build(HttpStatus.BAD_REQUEST, "Bad Request", msg, req, e.getClass().getSimpleName());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String,String>> handleDataAccess(DataAccessException e, HttpServletRequest req) {
        String root = Optional.of(e.getMostSpecificCause()).map(Throwable::getMessage).orElse(e.getMessage());
        return build(HttpStatus.CONFLICT, "Conflict: Data error", root, req, "DataAccessException");
    }

    @ExceptionHandler(ErrorResponseException.class) // incluye ResponseStatusException
    public ResponseEntity<Map<String,String>> handleErrorResponse(ErrorResponseException e, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String msg = e.getBody().getDetail() != null ? e.getBody().getDetail() : e.getMessage();
        return build(status, status.getReasonPhrase(), msg, req, e.getClass().getSimpleName());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e, HttpServletRequest req) {
        String msg = "An unexpected error occurred";
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", msg, req, "Exception");
    }
}
