package io.github.tuannh982.spring.boot.arch.router.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tuannh982.spring.boot.arch.router.rest.response.GeneralResponse;
import io.github.tuannh982.spring.boot.arch.router.rest.response.ResponseStatus;
import io.github.tuannh982.spring.boot.arch.router.rest.response.ResponseStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-2)
public final class GlobalRouterExceptionHandler implements WebExceptionHandler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DefaultDataBufferFactory DATA_BUFFER_FACTORY = new DefaultDataBufferFactory();
    private static final DataBuffer EMPTY_BUFFER = DATA_BUFFER_FACTORY.wrap(new byte[0]);

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        ServerHttpResponse response = serverWebExchange.getResponse();
        if (!this.updateResponse(response, throwable)) {
            return Mono.error(throwable);
        } else {
            String logPrefix = serverWebExchange.getLogPrefix();
            log.debug(logPrefix + this.formatError(throwable, serverWebExchange.getRequest()));
        }
        return response.setComplete();
    }

    private String formatError(Throwable ex, ServerHttpRequest request) {
        String reason = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        String path = request.getURI().getRawPath();
        return "Resolved [" + reason + "] for HTTP " + request.getMethod() + " " + path;
    }

    private boolean updateResponse(ServerHttpResponse response, Throwable throwable) {
        boolean result = false;
        HttpStatus status = this.determineStatus(throwable);
        if (status != null) {
            if (response.setStatusCode(status)) {
                handle(throwable, response);
                result = true;
            }
        } else {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                result = this.updateResponse(response, cause);
            }
        }
        return result;
    }

    // handling code here ----------------------------------------------------------------------------------------------

    private HttpStatus determineStatus(Throwable throwable) {
        if (throwable instanceof ResponseStatusException) {
            return ((ResponseStatusException) throwable).getStatus();
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private void handle(Throwable throwable, ServerHttpResponse response) {
        if (throwable instanceof ResponseStatusException) {
            handleResponseStatusException((ResponseStatusException) throwable, response);
        } else {
            handleUnExpectedException((Exception) throwable, response);
        }
    }

    private void handleResponseStatusException(ResponseStatusException ex, ServerHttpResponse response) {
        ex.getResponseHeaders().forEach((name, values) -> values.forEach(value -> response.getHeaders().add(name, value)));
    }

    private void handleUnExpectedException(Exception ex, ServerHttpResponse response) {
        ResponseStatusCode statusCode = ResponseStatusCode.INTERNAL_ERROR;
        ResponseStatus responseStatus = new ResponseStatus(statusCode.getResponseCode(), ex.getMessage());
        GeneralResponse<Object> responseData = new GeneralResponse<>(responseStatus, null);
        DataBuffer dataBuffer = null;
        try {
            dataBuffer = DATA_BUFFER_FACTORY.wrap(OBJECT_MAPPER.writeValueAsBytes(responseData));
        } catch (JsonProcessingException e) {
            dataBuffer = EMPTY_BUFFER;
            log.error(e.getMessage(), e);
        }
        response.writeWith(Mono.just(dataBuffer));
    }
}
