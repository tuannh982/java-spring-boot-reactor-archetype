package io.github.tuannh982.spring.boot.arch.router.rest;

import io.github.tuannh982.spring.boot.arch.router.rest.response.GeneralResponse;
import io.github.tuannh982.spring.boot.arch.router.rest.response.ResponseStatus;
import io.github.tuannh982.spring.boot.arch.router.rest.response.ResponseStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public final class GlobalRouterExceptionHandler {
    /**
     * Note that Spring WebFlux does not have an equivalent for the Spring MVC ResponseEntityExceptionHandler,
     * because WebFlux raises only ResponseStatusException (or subclasses thereof),
     * and those do not need to be translated to an HTTP status code.
     */
    @ExceptionHandler({ResponseStatusException.class})
    public <T> ResponseEntity<GeneralResponse<T>> handleRestException(ResponseStatusException ex, WebRequest request) {
        ResponseStatus responseStatus = new ResponseStatus("HTTP_" + ex.getStatus().value(), ex.getMessage());
        GeneralResponse<T> response = new GeneralResponse<>(responseStatus, null);
        return new ResponseEntity<>(response, ex.getResponseHeaders(), ex.getStatus());
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public <T> ResponseEntity<GeneralResponse<T>> handleUnExpectedException(Exception ex, WebRequest request) {
        ResponseStatusCode statusCode = ResponseStatusCode.INTERNAL_ERROR;
        ResponseStatus responseStatus = new ResponseStatus(statusCode.getResponseCode(), ex.getMessage());
        GeneralResponse<T> response = new GeneralResponse<>(responseStatus, null);
        return ResponseEntity.status(statusCode.getHttpCode()).body(response);
    }
}
