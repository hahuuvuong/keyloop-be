package com.keyloop.scheduler.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(SchedulerException.class)
    ResponseEntity<ProblemDetail> scheduler(SchedulerException e, HttpServletRequest r) {
        var status = switch (e.code()) {
            case INVALID_REQUEST, IDEMPOTENCY_KEY_REUSED -> HttpStatus.BAD_REQUEST;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case BOOKING_CONFLICT, BOOKING_RACE_LOST -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return response(status, e.code(), e.getMessage(), r);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class, IllegalArgumentException.class, HttpMessageNotReadableException.class, MissingRequestHeaderException.class})
    ResponseEntity<ProblemDetail> invalid(Exception e, HttpServletRequest r) {
        return response(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "Request validation failed", r);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception e, HttpServletRequest r) {
        log.error("Unexpected request failure path={} exception={}", r.getRequestURI(), e.getClass().getName(), e);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "An unexpected error occurred", r);
    }

    private ResponseEntity<ProblemDetail> response(HttpStatus status, ErrorCode code, String detail, HttpServletRequest req) {
        var p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setTitle(status.getReasonPhrase());
        p.setType(URI.create("https://keyloop.example/problems/" + code.name().toLowerCase().replace('_', '-')));
        p.setInstance(URI.create(req.getRequestURI()));
        p.setProperty("code", code.name());
        p.setProperty("timestamp", Instant.now());
        p.setProperty("correlationId", MDC.get("correlationId"));
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(p);
    }
}
