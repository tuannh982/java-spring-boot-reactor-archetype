package io.github.tuannh982.spring.boot.reactor.arch.exception;

import lombok.Setter;

import java.util.Map;

@Setter
public abstract class DomainException extends Exception {
    private Map<String, String> values;
    private Object data;

    protected DomainException(Map<String, String> values, Object data) {
        this.values = values;
        this.data = data;
    }

    protected DomainException() {
        this.values = null;
        this.data = null;
    }

    public abstract String code();

    public Map<String, String> values() {
        return values;
    }

    public Object data() {
        return data;
    }
}
