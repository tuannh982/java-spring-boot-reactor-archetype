package io.github.tuannh982.spring.boot.reactor.arch.router.rest.response;

import io.github.tuannh982.spring.boot.reactor.arch.commons.text.TextUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class HttpResponseGenerator {
    private final ResourceBundleMessageSource errorMessageBundle;

    public HttpResponseGenerator(@Qualifier("error-message-bundle") ResourceBundleMessageSource errorMessageBundle) {
        this.errorMessageBundle = errorMessageBundle;
    }

    public ResponseStatus responseStatus(String code, Locale locale, Map<String, String> values) {
        String template = errorMessageBundle.getMessage(code, null, locale);
        if (values == null) {
            return new ResponseStatus(code, template);
        } else {
            return new ResponseStatus(code, TextUtils.substitute(template, values));
        }
    }

    public <T> ResponseEntity<GeneralResponse<T>> success(T data, Locale locale, Map<String, String> values) {
        ResponseStatusCode responseStatusCode = BaseResponseStatusCode.SUCCESS;
        GeneralResponse<T> response = new GeneralResponse<>(responseStatus(responseStatusCode.getResponseCode(), locale, values), data);
        return ResponseEntity.status(responseStatusCode.getHttpCode()).body(response);
    }

    public <T> ResponseEntity<GeneralResponse<T>> success(T data, Locale locale) {
        return success(data, locale, null);
    }

    public <T> ResponseEntity<GeneralResponse<T>> fail(ResponseStatusCode responseStatusCode, T data, Locale locale, Map<String, String> values) {
        GeneralResponse<T> response = new GeneralResponse<>(responseStatus(responseStatusCode.getResponseCode(), locale, values), data);
        return ResponseEntity.status(responseStatusCode.getHttpCode()).body(response);
    }

    public <T> ResponseEntity<GeneralResponse<T>> fail(ResponseStatusCode responseStatusCode, Locale locale, Map<String, String> values) {
        return fail(responseStatusCode, null, locale, values);
    }

    public <T> ResponseEntity<GeneralResponse<T>> fail(ResponseStatusCode responseStatusCode, Locale locale) {
        return fail(responseStatusCode, null, locale, null);
    }

    public <T> ResponseEntity<GeneralResponse<T>> fail(ResponseStatusCode responseStatusCode, T data, Locale locale) {
        return fail(responseStatusCode, data, locale, null);
    }
}
