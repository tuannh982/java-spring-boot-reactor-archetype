package io.github.tuannh982.spring.boot.reactor.arch.router.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tuannh982.spring.boot.reactor.arch.commons.text.TextUtils;
import io.github.tuannh982.spring.boot.reactor.arch.exception.DomainException;
import io.github.tuannh982.spring.boot.reactor.arch.router.rest.response.BaseResponseStatusCode;
import io.github.tuannh982.spring.boot.reactor.arch.router.rest.response.GeneralResponse;
import io.github.tuannh982.spring.boot.reactor.arch.router.rest.response.ResponseStatus;
import io.github.tuannh982.spring.boot.reactor.arch.router.rest.response.ResponseStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
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

import java.util.Locale;

@Slf4j
@Component
@Order(-2)
public final class GlobalRouterExceptionHandler implements WebExceptionHandler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DefaultDataBufferFactory DATA_BUFFER_FACTORY = new DefaultDataBufferFactory();
    private static final DataBuffer EMPTY_BUFFER = DATA_BUFFER_FACTORY.wrap(new byte[0]);

    private final ResourceBundleMessageSource errorMessageBundle;

    public GlobalRouterExceptionHandler(@Qualifier("error-message-bundle") ResourceBundleMessageSource errorMessageBundle) {
        this.errorMessageBundle = errorMessageBundle;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        ServerHttpResponse response = serverWebExchange.getResponse();
        Locale locale = serverWebExchange.getLocaleContext().getLocale();
        Mono<Void> ret = this.updateResponse(response, locale, throwable);
        log.debug("{} throws exception {}", serverWebExchange.getRequest().getURI().getRawPath(), throwable);
        return ret;
    }

    private Mono<Void> updateResponse(ServerHttpResponse response, Locale locale, Throwable throwable) {
        HttpStatus status = this.determineStatus(throwable);
        if (status != null) {
            if (response.setStatusCode(status)) {
                return handle(throwable, response, locale);
            }
        } else {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                return this.updateResponse(response, locale, cause);
            }
        }
        return Mono.error(throwable);
    }

    // handling code here ----------------------------------------------------------------------------------------------

    private HttpStatus determineStatus(Throwable throwable) {
        if (throwable instanceof ResponseStatusException) {
            return ((ResponseStatusException) throwable).getStatus();
        } else if (throwable instanceof DomainException) {
            return HttpStatus.OK;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private Mono<Void> handle(Throwable throwable, ServerHttpResponse response, Locale locale) {
        if (throwable instanceof ResponseStatusException) {
            return handleResponseStatusException((ResponseStatusException) throwable, response);
        } else if (throwable instanceof DomainException) {
            return handleDomainException((DomainException) throwable, response, locale);
        } else {
            return handleUnExpectedException((Exception) throwable, response);
        }
    }

    private Mono<Void> handleResponseStatusException(ResponseStatusException ex, ServerHttpResponse response) {
        ex.getResponseHeaders().forEach((name, values) -> values.forEach(value -> response.getHeaders().add(name, value)));
        return Mono.empty();
    }

    private Mono<Void> handleDomainException(DomainException ex, ServerHttpResponse response, Locale locale) {
        String message = ex.getMessage();
        ResponseStatus responseStatus = null;
        if (StringUtils.isEmpty(message)) {
            message = errorMessageBundle.getMessage(ex.code(), null, locale);
            if (ex.values() == null) {
                responseStatus = new ResponseStatus(ex.code(), message);
            } else {
                responseStatus = new ResponseStatus(ex.code(), TextUtils.substitute(message, ex.values()));
            }
        } else {
            responseStatus = new ResponseStatus(ex.code(), message);
        }
        GeneralResponse<Object> responseData = new GeneralResponse<>(responseStatus, ex.data());
        DataBuffer dataBuffer = null;
        try {
            dataBuffer = DATA_BUFFER_FACTORY.wrap(OBJECT_MAPPER.writeValueAsBytes(responseData));
        } catch (JsonProcessingException e) {
            dataBuffer = EMPTY_BUFFER;
            log.error(e.getMessage(), e);
        }
        return response.writeWith(Mono.just(dataBuffer));
    }

    private Mono<Void> handleUnExpectedException(Exception ex, ServerHttpResponse response) {
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
        return response.writeWith(Mono.just(dataBuffer));
    }
}
